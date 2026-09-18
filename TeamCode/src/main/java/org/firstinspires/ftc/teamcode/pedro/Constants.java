package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * PEDRO 3 CONSTANTS — first pass, NOT tuned yet.
 *
 * Motor names/directions below are copied straight from your existing
 * Drivetrain.java ("fl"/"bl"/"fr"/"br", FL+BL reversed, FR+BR forward).
 * Pinpoint pod offsets/directions are copied from your existing Pinpoint.java
 * (4.5, -7.125 in; FORWARD / REVERSED). VERIFY these are still correct for
 * the new bot — offsets in particular are physical measurements from the
 * DECODE chassis and almost certainly need re-measuring on the new one.
 *
 * ForesightConfig below is a PLACEHOLDER copied from Pedro's own docs
 * example — every number in it (translational gains, brake coefficients,
 * max velocities) is specific to team 12808's robot, not yours. Do not
 * drive/compete on these values. They exist only so this file compiles
 * before you run AutoTune. Replace this whole block once tuning is done —
 * see https://pedropathing.com/docs/pathing/tuning
 */
public class Constants {

    // ── Drivetrain ──────────────────────────────────────────────────────────
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("fl");
        c.frontRightName.set("fr");
        c.backLeftName.set("bl");
        c.backRightName.set("br");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });
    // ── Localizer (Pinpoint) ───────────────────────────────────────────────
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(9.42380890132874);
        c.yPodOffset.set(-5.382967971441315);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // ── Foresight (motion algorithm) — PLACEHOLDER, not yours yet ─────────
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward   = Controller.proportional(0.3);
                Controller secondaryTranslationalForward = Controller.proportional(0.1);
                Controller primaryTranslationalLateral   = Controller.proportional(0.3);
                Controller secondaryTranslationalLateral = Controller.proportional(0.1);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.011));
                c.brake.set(Controller.proportionalFeedforward(0.009));

                c.headingFeedback.set(Controller.proportional(5.0));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.056, 0.006));

                c.linearBrakeCoefficients.set(Matrix.diag(0.106, 0.087));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0015, 0.0014));

                // These four especially MUST come from your own AutoTune run —
                // they're top speed / deceleration numbers, entirely robot-specific.
                c.maxAchievableForwardVelocity.set(70.0);
                c.maxAchievableStrafeVelocity.set(50.0);
                c.naturalForwardDeceleration.set(80.0);
                c.naturalStrafeDeceleration.set(100.0);
            }
    );

    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
}