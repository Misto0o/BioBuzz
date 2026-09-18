package org.firstinspires.ftc.teamcode.teleop;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Robot.Drivetrain;
import org.firstinspires.ftc.teamcode.Robot.Pinpoint;
import org.firstinspires.ftc.teamcode.Vision.Limelight;

/**
 * Minimal driving TeleOp — PORTED FROM DECODE, STRIPPED DOWN.
 *
 * IMPORTANT — this version fixes a mismatch from the first draft: your real
 * Drivetrain class (org.firstinspires.ftc.teamcode.Robot.Drivetrain) is NOT a
 * singleton — it has no getInstance() and no no-arg init(), it's built with
 * `new Drivetrain(hardwareMap)` directly in its constructor, and it has no
 * getTurnSpeed() method. So here it's held as an instance field and
 * constructed in init(), and turn speed is a local @Config constant instead.
 * Pinpoint is assumed to still be the singleton style (Pinpoint.INSTANCE) —
 * swap that too if yours turns out not to be built that way.
 *
 * What's gone: Intake / Spindexer / Transfer / Turret / DistanceSensor /
 * MatchPattern and every state machine built around them (flick, auto-shoot,
 * rescan) — DECODE-specific, not ported.
 *
 * What's kept, unchanged in behavior:
 *  - Pinpoint odometry, updated every loop
 *  - Limelight init + setRobotOrientation feed each loop
 *  - DPad UP: reset odometry to the alliance starting corner
 *  - DPad DOWN: manual snap — 5-sample averaged Limelight MT1 pose,
 *    bounds-checked, relocalizes Pinpoint's full pose (x, y, heading).
 *    Same rumble feedback as DECODE.
 */
@Config
@TeleOp(name = "Teleop", group = "MAIN")
public class Teleop extends OpMode {

    // Drivetrain has no getTurnSpeed() of its own — tune this here instead.
    public static double TURN_SPEED = 0.7;

    private enum Alliance { BLUE, RED }
    private Alliance alliance = Alliance.BLUE;

    private Drivetrain drivetrain;

    private boolean lastDU, lastDD;

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        drivetrain = new Drivetrain(hardwareMap);
        Pinpoint.INSTANCE.init(hardwareMap);
        Limelight.INSTANCE.initialize(hardwareMap);

        telemetry.addLine("DPad UP = Blue  |  DPad DOWN = Red  (during init)");
        telemetry.update();
    }

    @Override
    public void init_loop() {
        boolean du = gamepad1.dpad_up, dd = gamepad1.dpad_down;
        if (du && !lastDU) alliance = Alliance.BLUE;
        if (dd && !lastDD) alliance = Alliance.RED;
        lastDU = du; lastDD = dd;

        Limelight.INSTANCE.setRobotOrientation(Pinpoint.INSTANCE.getHeading());
        double[] mt1 = Limelight.INSTANCE.getMT1Pose();
        if (mt1 != null) {
            Pinpoint.INSTANCE.updatePosition(new Pose2D(
                    DistanceUnit.INCH, mt1[0], mt1[1],
                    AngleUnit.DEGREES, Pinpoint.INSTANCE.getHeading()));
            telemetry.addData("Start Pos (MT1)", String.format("(%.1f, %.1f)", mt1[0], mt1[1]));
        } else {
            double fallbackX = (alliance == Alliance.BLUE) ? 135.5 : 8.5;
            Pinpoint.INSTANCE.updatePosition(new Pose2D(
                    DistanceUnit.INCH, fallbackX, 9,
                    AngleUnit.DEGREES, Pinpoint.INSTANCE.getHeading()));
            telemetry.addData("Start Pos (fallback)", String.format("(%.1f, 9)", fallbackX));
        }

        telemetry.addData("Alliance", alliance);
        telemetry.update();
    }

    @Override
    public void start() {
        Limelight.INSTANCE.setRobotOrientation(Pinpoint.INSTANCE.getHeading());
        double[] mt1Start = Limelight.INSTANCE.getAveragedSnapshotPose(5);
        if (mt1Start != null) {
            Pinpoint.INSTANCE.updatePosition(new Pose2D(
                    DistanceUnit.INCH, mt1Start[0], mt1Start[1],
                    AngleUnit.DEGREES, Pinpoint.INSTANCE.getHeading()));
        }
    }

    @Override
    public void loop() {
        Pinpoint.INSTANCE.periodic();
        Limelight.INSTANCE.setRobotOrientation(Pinpoint.INSTANCE.getHeading());

        boolean du = gamepad1.dpad_up;
        boolean dd = gamepad1.dpad_down;

        // ── DPad UP: reset odometry to alliance corner ──────────────────────
        if (du && !lastDU) {
            Pinpoint.INSTANCE.updatePosition(alliance == Alliance.BLUE
                    ? new Pose2D(DistanceUnit.INCH, 135.5, 9, AngleUnit.DEGREES, 0)
                    : new Pose2D(DistanceUnit.INCH, 8.5,   9, AngleUnit.DEGREES, 180));
        }

        // ── DPad DOWN: manual snap-to-vision relocalization ─────────────────
        if (dd && !lastDD) {
            double[] visionPose = Limelight.INSTANCE.getAveragedSnapshotPose(5);
            double visionYaw = Limelight.INSTANCE.getMT1Yaw();
            if (visionPose != null && visionYaw >= 0) {
                double visionX = visionPose[0];
                double visionY = visionPose[1];
                if (visionX > 1.0 && visionX < 143.0 && visionY > 1.0 && visionY < 143.0) {
                    Pinpoint.INSTANCE.relocalizeFull(visionX, visionY, visionYaw);
                    gamepad1.rumble(1.0, 1.0, 150);
                } else {
                    gamepad1.rumble(0.5, 0.0, 400);
                }
            } else {
                gamepad1.rumble(0.5, 0.0, 400);
            }
        }

        lastDU = du;
        lastDD = dd;

        // ── Drivetrain ────────────────────────────────────────────────────
        drivetrain.drive(
                -gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                gamepad1.right_stick_x * TURN_SPEED
        );

        // ── Telemetry ─────────────────────────────────────────────────────
        double px = Pinpoint.INSTANCE.getPosX();
        double py = Pinpoint.INSTANCE.getPosY();
        double[] mt1 = Limelight.INSTANCE.getMT1Pose();

        telemetry.addData("Alliance", alliance);
        telemetry.addData("Pinpoint",
                String.format("(%.1f, %.1f) %.1f°", px, py, Pinpoint.INSTANCE.getHeading()));
        telemetry.addData("MT1", mt1 != null ? String.format("(%.1f, %.1f)", mt1[0], mt1[1]) : "NO DATA");
        telemetry.update();
    }

    @Override
    public void stop() {
        // No shooter/intake hardware left to shut down.
    }
}