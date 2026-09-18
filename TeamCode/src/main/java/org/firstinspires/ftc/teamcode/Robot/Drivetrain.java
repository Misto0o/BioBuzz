package org.firstinspires.ftc.teamcode.Robot;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.IMU;

public class Drivetrain {

    private DcMotorEx frontLeftMotor;
    private DcMotorEx backLeftMotor;
    private DcMotorEx frontRightMotor;
    private DcMotorEx backRightMotor;

    private IMU imu;

    public Drivetrain(HardwareMap hardwareMap) {
        frontLeftMotor  = hardwareMap.get(DcMotorEx.class, "fl");
        backLeftMotor   = hardwareMap.get(DcMotorEx.class, "bl");
        frontRightMotor = hardwareMap.get(DcMotorEx.class, "fr");
        backRightMotor  = hardwareMap.get(DcMotorEx.class, "br");

        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.FORWARD);

        // --- IMU setup (built-in Control Hub IMU) ---
        // NOTE: confirm logo/USB direction match your actual hub mounting.
        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parameters = new IMU.Parameters(
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
                )
        );
        imu.initialize(parameters);
    }

    public void resetHeading() {
        imu.resetYaw();
    }

    public double getHeading() {
        return imu.getRobotYawPitchRollAngles().getYaw(org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS);
    }

    /** Robot-centric drive (unchanged, kept in case you need it) */
    public void drive(double y, double x, double rx) {
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        frontLeftMotor.setPower( (y + x + rx) / denominator);
        backLeftMotor.setPower(  (y - x + rx) / denominator);
        frontRightMotor.setPower((y - x - rx) / denominator);
        backRightMotor.setPower( (y + x - rx) / denominator);
    }

    /** Field-centric drive: rotates x/y by current heading before mecanum math */
    public void driveFieldCentric(double y, double x, double rx) {
        double heading = getHeading();

        double rotX = x * Math.cos(-heading) - y * Math.sin(-heading);
        double rotY = x * Math.sin(-heading) + y * Math.cos(-heading);

        double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rx), 1);
        frontLeftMotor.setPower( (rotY + rotX + rx) / denominator);
        backLeftMotor.setPower(  (rotY - rotX + rx) / denominator);
        frontRightMotor.setPower((rotY - rotX - rx) / denominator);
        backRightMotor.setPower( (rotY + rotX - rx) / denominator);
    }
}