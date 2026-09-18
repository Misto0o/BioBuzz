package org.firstinspires.ftc.teamcode.Vision;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;

import java.util.List;

/**
 * Limelight Vision Subsystem — BIOBUZZ (2026-2027).
 *
 * REVERTED FROM KALMAN BACK TO PLAIN DECODE-STYLE MT1 — decision made after
 * Kickoff: BIOBUZZ's only AprilTags are on the underside of the HIVE's two
 * CELLs, facing DOWN, and only the currently-down CELL's tag is visible at
 * any moment (it flips every HIVE TIP). That's sparse, near-field-only,
 * intermittent data — a Kalman filter's whole value is squeezing more out of
 * *frequent* noisy corrections, which this season doesn't have. The old
 * DECODE-style single-shot blend (see Teleop's dpad-down handler:
 * 0.95*pinpoint + 0.05*limelight on sighting) does the same job with far
 * less to tune and far less to break under match pressure.
 *
 * MT1, not MT2 — MT2 needs a fed-in heading to disambiguate a tag, which
 * matters most when you're relying on vision continuously. For occasional
 * near-hive snaps, MT1's simplicity wins; there's no continuous fusion loop
 * depending on razor-accurate real-time yaw feed anymore.
 *
 * BIOBUZZ AprilTag IDs (confirmed from manual Section 9.9, Figure 9-17):
 *   Red  CELL, far side  ("Red Scoring Tags")   : 30, 31, 32, 33
 *   Red  CELL, audience side ("Red Audience")   : 34, 35, 36, 37
 *   Blue CELL, audience side ("Blue Audience")  : 38, 39, 40, 41
 *   Blue CELL, far side ("Blue Scoring Tags")   : 42, 43, 44, 45
 * "Scoring"/"Audience" here names a fixed FIELD SIDE, not "up" vs "down" —
 * either cluster can be the currently-down (visible) one depending on which
 * way that alliance's HIVE happens to be tipped. Don't conflate the two.
 * There is no longer a single TARGET_TAG_ID — hasTarget()/distanceFromTag()
 * take whichever ID you care about from the ranges above.
 * Note MT1 localization itself (getMT1Pose/getSnapshotPose) does NOT need
 * any of these IDs — it just needs the correct field map (.fmap) loaded on
 * the Limelight's own web UI, which is a separate, Limelight-side setup step.
 *
 * Everything below is otherwise unchanged from DECODE: hasTarget,
 * distanceFromTag, the MT1 pose/yaw pipeline, and the snapshot averaging
 * helpers used by Teleop's manual snap. Do not touch getMT1Pose()'s
 * transform steps — that ordering is confirmed working on-robot and is a
 * Limelight-mounting/field-origin transform, not a goal-relative one, so
 * it's not season-dependent.
 */
@Config
public class    Limelight {
    public static final Limelight INSTANCE = new Limelight();
    private Limelight() {}

    private Limelight3A limelight;

    // Camera mounting constants — measured from CAD. RE-MEASURE for the new
    // mount: the camera is going upward-facing on the new bot per the
    // post-kickoff decision (BIOBUZZ tags face down from the HIVE), a
    // completely different geometry than these DECODE placeholders assume.
    public static double CAMERA_HEIGHT_IN = 17.5;
    public static double TAG_HEIGHT_IN    = 29.5;
    public static double CAMERA_TILT_DEG  = 12.07;

    public static double CAMERA_RADIUS_INCHES = 6.0;

    // ── BIOBUZZ AprilTag ID ranges (confirmed, see class-level comment) ────
    public static final int RED_SCORING_TAG_MIN  = 30, RED_SCORING_TAG_MAX  = 33;
    public static final int RED_AUDIENCE_TAG_MIN = 34, RED_AUDIENCE_TAG_MAX = 37;
    public static final int BLUE_AUDIENCE_TAG_MIN = 38, BLUE_AUDIENCE_TAG_MAX = 41;
    public static final int BLUE_SCORING_TAG_MIN  = 42, BLUE_SCORING_TAG_MAX  = 45;

    /** True if the given fiducial ID falls in [min, max] inclusive. */
    private static boolean inRange(int id, int min, int max) { return id >= min && id <= max; }

    public boolean hasTargetInRange(int min, int max) {
        LLResult latest = (limelight == null) ? null : limelight.getLatestResult();
        if (latest == null) return false;
        List<LLResultTypes.FiducialResult> r = latest.getFiducialResults();
        if (r == null || r.isEmpty()) return false;
        for (LLResultTypes.FiducialResult fiducial : r) {
            if (inRange(fiducial.getFiducialId(), min, max)) return true;
        }
        return false;
    }

    // ── Pipeline indices ──────────────────────────────────────────────────────
    public static final int DETECTOR_PIPELINE = 9;
    public static final int FIDUCIAL_PIPELINE = 0;

    // ─────────────────────────────────────────────────────────────────────────
    public void initialize(HardwareMap hardwareMap) {
        try {
            limelight = hardwareMap.get(Limelight3A.class, "limelight");
            limelight.setPollRateHz(100);
            limelight.pipelineSwitch(FIDUCIAL_PIPELINE);
            limelight.start();
        } catch (Exception e) {
            limelight = null;
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    public void start() {
        if (limelight != null) limelight.start();
    }

    public void stop() {
        // Intentionally keep the camera running to avoid restart latency.
    }

    // ── Pipeline switching ────────────────────────────────────────────────────
    public void switchToDetector()  { if (limelight != null) limelight.pipelineSwitch(DETECTOR_PIPELINE); }
    public void switchToFiducial()  { if (limelight != null) limelight.pipelineSwitch(FIDUCIAL_PIPELINE); }

    public int getCurrentPipeline() {
        if (limelight == null) return -1;
        LLResult r = limelight.getLatestResult();
        return (r != null) ? r.getPipelineIndex() : -1;
    }

    public LLResult getRawResult() {
        if (limelight == null) return null;
        return limelight.getLatestResult();
    }

    // ── Fiducial helpers ──────────────────────────────────────────────────────

    /**
     * Returns true if the given tag ID is currently visible.
     * Use this instead of checking getTx() == 0, because tx can legitimately
     * be zero when the tag IS visible but perfectly centered.
     *
     * BIOBUZZ note: since only the currently-down CELL's tag faces the
     * tiles, hasTarget(id) on that tag's ID is a cheap way to ask "is this
     * the down CELL right now" — a game-state check, not just a vision check.
     */
    public boolean hasTarget(int tagID) {
        if (limelight == null) return false;
        LLResult latest = limelight.getLatestResult();
        if (latest == null) return false;
        List<LLResultTypes.FiducialResult> r = latest.getFiducialResults();
        if (r == null || r.isEmpty()) return false;
        for (LLResultTypes.FiducialResult fiducial : r) {
            if (fiducial.getFiducialId() == tagID) return true;
        }
        return false;
    }

    /**
     * Returns estimated distance to the tag (inches).
     * Uses camera-to-tag pose from the LL, plus a fixed +16 in offset
     * on both axes to account for camera mounting position.
     * Returns 0 if tag is not visible.
     * NOTE: the +16in fudge factor is DECODE's camera mount offset — must be
     * re-measured for the new upward-facing mount before trusting this value.
     */
    public double distanceFromTag(int tagID) {
        if (limelight == null) return 0;
        LLResult latest = limelight.getLatestResult();
        if (latest == null) return 0;
        List<LLResultTypes.FiducialResult> r = latest.getFiducialResults();
        if (r == null || r.isEmpty()) return 0;
        for (LLResultTypes.FiducialResult fiducial : r) {
            if (fiducial.getFiducialId() == tagID) {
                double x = (fiducial.getCameraPoseTargetSpace().getPosition().x / DistanceUnit.mPerInch) + 16;
                double z = (fiducial.getCameraPoseTargetSpace().getPosition().z / DistanceUnit.mPerInch) + 16;
                return Math.sqrt(x * x + z * z);
            }
        }
        return 0;
    }

    // ── MT1 -> field-corner-origin (Pinpoint-style) conversion ─────────────
    // UNCHANGED FROM DECODE — confirmed working on-robot, not season-dependent.
    // Steps, in order, exactly as before:
    //   1. swap X and Y
    //   2. add 72 to X
    //   3. negate Y
    //   4. add 72 to Y
    //   5. heading: normalize to [0,360), then subtract 90
    // Do NOT reorder these steps — swap-then-shift is not the same transform as
    // shift-then-swap.
    public double[] getMT1Pose() {
        if (limelight == null) return null;

        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) return null;

        Pose3D botpose = result.getBotpose();
        if (botpose == null) return null;
        if (result.getFiducialResults() == null || result.getFiducialResults().isEmpty()) return null;

        Position posIn = botpose.getPosition().toUnit(DistanceUnit.INCH);
        double rawX = posIn.x;
        double rawY = posIn.y;

        if (Math.abs(rawX) < 0.01 && Math.abs(rawY) < 0.01) return null;

        // Step 1: swap
        double swappedX = rawY;
        double swappedY = rawX;

        // Step 2 & 3 & 4
        double fieldX = swappedX + 72.0;
        double fieldY = (-swappedY) + 72.0;

        if (fieldX < 0 || fieldX > 144 || fieldY < 0 || fieldY > 144) return null;

        return new double[]{ fieldX, fieldY };
    }

    /**
     * MT1 heading, using the same normalize-then-minus-90 convention as the
     * confirmed-working transform. Degrees.
     */
    public double getMT1Yaw() {
        if (limelight == null) return -1;
        LLResult result = limelight.getLatestResult();
        if (result == null) return -1;

        Pose3D botpose = result.getBotpose();
        if (botpose == null) return -1;

        double yaw = botpose.getOrientation().getYaw(AngleUnit.DEGREES);
        if (yaw < 0) yaw += 360;
        yaw -= 90;
        return yaw;
    }

    /**
     * MT1 raw pose in inches, NO conversion applied — straight from the LL.
     * Use this to sanity-check getMT1Pose()'s transform against known field spots.
     */
    public double[] getMT1PoseRaw() {
        if (limelight == null) return null;

        LLResult result = limelight.getLatestResult();
        if (result == null) return null;

        Pose3D botpose = result.getBotpose();
        if (botpose == null) return null;

        Position posIn = botpose.getPosition().toUnit(DistanceUnit.INCH);
        return new double[]{ posIn.x, posIn.y };
    }

    // ── Snapshot helpers used by Teleop's manual dpad-down snap ──────────────

    public double[] getSnapshotPose() {
        return getMT1Pose();
    }

    public double[] getAveragedSnapshotPose(int samples) {
        double sumX = 0, sumY = 0;
        int valid = 0;
        for (int i = 0; i < samples; i++) {
            double[] pose = getSnapshotPose();
            if (pose != null && pose[0] > 1 && pose[0] < 143 && pose[1] > 1 && pose[1] < 143) {
                sumX += pose[0];
                sumY += pose[1];
                valid++;
            }
            try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        if (valid == 0) return null;
        return new double[]{ sumX / valid, sumY / valid };
    }

    public void setRobotOrientation(double yawDegrees) {
        if (limelight != null) {
            limelight.updateRobotOrientation(yawDegrees);
        }
    }
}