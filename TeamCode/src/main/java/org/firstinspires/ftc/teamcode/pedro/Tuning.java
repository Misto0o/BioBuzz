package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.PinpointTuner;

/**
 * AutoTune procedure registry.
 *
 * Every method annotated @Tuner here shows up as a selectable procedure on
 * AutoTune's web page (http://192.168.43.1:10158 while connected to the
 * robot). Previously only pinpointTuner() was registered, which is why
 * Pinpoint was the only option that appeared.
 *
 * NOTE: this is NOT the old DECODE-era Tuning class. Pedro 3 replaced the
 * @TeleOp SelectableOpMode menu (LocalizationTest / ForwardVelocityTuner /
 * etc. picked from the Driver Station) with this AutoTune web interface.
 * That's why "Tuning" never showed up in the Driver Station OpMode list —
 * it isn't supposed to be there. Run everything from the web page.
 *
 * RUN ORDER:
 *   1. Mecanum Tuner   — confirms motor names + spin directions
 *   2. Pinpoint Tuner  — pod directions + X/Y offsets (already done once)
 *   3. Foresight Tuner — the big one: velocities, decelerations, braking
 *                        coefficients, and translational/heading kP values.
 *                        This is what replaces the placeholder numbers
 *                        currently sitting in Constants.foresightConfig.
 *
 * Each procedure ends by printing a ready-to-paste Java config block on the
 * AutoTune page's "Java" tab — copy those straight over the corresponding
 * block in Constants.java.
 *
 * Only procedures matching this robot's hardware are registered. The other
 * localizer procedures in procedures/ (OctoQuadTuner, OTOSTuner,
 * ThreeWheelTuner, ThreeWheelIMUTuner, TwoWheelTuner) are for sensors this
 * robot doesn't have, so they're intentionally left out.
 */
public class Tuning {

    /**
     * Drivetrain motor names and directions.
     * Takes no constructor args — it asks for the motor names interactively
     * on the AutoTune page, then spins each wheel one at a time so you can
     * report which way it turned.
     */
    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }

    /**
     * Localizer pod directions and offsets.
     * Already run — produced the offsets currently in Constants.java.
     * Re-run any time the pods get physically moved or remounted.
     */
    @Tuner
    public static Procedure pinpointTuner() {
        return new PinpointTuner();
    }

    /**
     * Foresight motion algorithm tuning.
     *
     * Takes two factory functions rather than finished objects: it spins up
     * a fresh Localizer and Drivetrain from the HardwareMap for each of the
     * many sub-OpModes it runs internally (ForwardVelocity, StrafeVelocity,
     * ForwardDeceleration, HeadingBraking, ForwardBraking, etc.), so it
     * needs to be able to construct them on demand rather than being handed
     * one shared instance.
     *
     * Both lambdas below build from the same Constants configs the Follower
     * uses, so this tunes against the identical hardware setup the robot
     * actually drives with.
     *
     * HEADS UP — this procedure drives the robot hard and far: full-power
     * forward and strafe runs, repeated accelerate/brake cycles, and
     * spin-in-place tests. It asks for a distance (default 48in) and the
     * braking step wants at least 15in. Clear real space on all four sides
     * before starting, and expect the robot to overshoot past the target
     * distance while coasting down.
     */
    @Tuner
    public static Procedure foresightTuner() {
        return new ForesightTuner(
                hardwareMap -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig),
                hardwareMap -> new Mecanum(hardwareMap, Constants.drivetrainConfig)
        );
    }
}