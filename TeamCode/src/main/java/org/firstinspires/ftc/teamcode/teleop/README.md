# 🎮 TeleOp Quickstart

This package contains our team's driver-controlled OpMode (`Teleop.java`).

## 🕹️ Driver Controls Cheat Sheet

| Button / Control                | Action                                          |
| ------------------------------- | ----------------------------------------------- |
| **Left Joystick (Up/Down)**     | Move robot forward / backward                   |
| **Left Joystick (Left/Right)**  | Strafe robot left / right                       |
| **Right Joystick (Left/Right)** | Turn robot left / right                         |
| **D-Pad UP (During Init)**      | Set alliance to **BLUE**                        |
| **D-Pad DOWN (During Init)**    | Set alliance to **RED**                         |
| **D-Pad UP (During Match)**     | Reset position tracking back to starting corner |
| **D-Pad DOWN (During Match)**   | Snap robot location using AprilTag camera       |

---

## ⚠️ Current Status — September 12, 2026

> **Kickoff has just ended, so some systems are not currently operational.**

The **Limelight vision system is currently NOT working** as of **9/12/2026**.

This is expected for the current state of the Robot In 5 Weeks build. The Limelight needs to be **recalibrated and configured for the new game**, and some of the other robot components that the vision system depends on **have not been created yet**.

Because of this, the **AprilTag camera position-snapping feature does not currently run**.

### 🟢 The Code Is Working

The TeleOp code itself is functional. The camera-related features simply **cannot be fully used yet** because the required hardware and configuration are still being developed.

Once the Limelight is recalibrated and the remaining robot systems are implemented, the position-snapping functionality can be brought online.

---

## 📍 Relocalizing the Robot

Until the Limelight snapping system is available, you can manually reset the robot's starting position:

1. Drive the robot into **one of the starting corners**.
2. Make sure the robot is positioned where you want its starting position to be.
3. Press **D-Pad UP**.
4. The robot's position tracking will be reset to that starting corner.

This allows the robot to be properly localized even while the Limelight system is unavailable.

---

## 🧠 How the Code Works

### `init()` — Wake Up

When the OpMode is initialized, it:

* Initializes the drivetrain motors.
* Initializes the Pinpoint localization system.
* Initializes the Limelight camera system.

### `init_loop()` — Pick Alliance

Before starting the match:

* Press **D-Pad UP** to select **BLUE Alliance**.
* Press **D-Pad DOWN** to select **RED Alliance**.

The selected alliance is used by the robot's localization and vision systems.

### `loop()` — Drive Time! 🎮

During the match, the OpMode continuously reads the gamepad and controls the robot:

* **Left joystick:** Forward/backward and strafing.
* **Right joystick:** Rotation.
* **D-Pad UP:** Reset localization to the starting corner.
* **D-Pad DOWN:** Attempt AprilTag-based position snapping.

> **Note:** AprilTag position snapping is currently unavailable until the Limelight is recalibrated and the remaining required robot systems are implemented.
