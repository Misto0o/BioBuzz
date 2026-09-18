# FTC 6183 Triple Paradox — Robot In 5 Weeks 🚀

This repository contains the software for **FTC Team 6183 Triple Paradox** for our **Robot In 5 Weeks (RI5W)** build.

The goal of this project is to create a simple, reliable, and functional robot with straightforward actions. This codebase will be updated and expanded throughout the 5-week build.

# 🏗️ Project Overview

* **Status:** Active 5-Week Development

## 📂 Repository Structure

```text
TeamCode/
├── pedro/      # Pedro Pathing setup, constants, and tuning
├── Robot/      # Hardware definitions and subsystem controls
├── teleop/     # Driver-controlled TeleOp OpModes
├── Utils/      # Utility classes, math helpers, and field constants
└── Vision/     # Vision processing and camera detection
```

### `pedro/`

Contains the core setup and configuration for **Pedro Pathing**, including constants and tuning files.

Pedro Pathing is used for autonomous navigation, path following, localization, and movement tuning.

### `Robot/`

Contains hardware initialization and subsystem definitions for the robot.

This package includes motor, servo, and sensor configuration along with methods for controlling the robot's mechanisms.

### `teleop/`

Contains the **TeleOp (driver-controlled) OpModes**.

This package handles gamepad inputs and maps joysticks, triggers, and buttons to robot movement and mechanism controls.

### `Utils/`

Contains general-purpose utility classes used throughout the project.

This includes mathematical calculations, helper functions, custom data structures, and field constants that help keep the main OpModes clean.

### `Vision/`

Contains the robot's vision processing and camera-related code.

This package handles detection and processing of targets, AprilTags, game pieces, or other objects used by autonomous and driver-controlled routines.

## 🛠️ Getting Started

### 1. Clone the Repository

Clone this repository to your local development environment and open the project in **Android Studio**.

### 2. Explore the Code

Each package is organized by its responsibility:

* `pedro/` — Autonomous navigation and tuning
* `Robot/` — Hardware and subsystems
* `teleop/` — Driver control
* `Utils/` — Shared utilities
* `Vision/` — Camera and vision processing

### 3. Configure the Robot

Make sure the **Driver Station configuration** matches the motor, servo, and sensor names defined in the robot hardware classes.

### 4. Build and Test

Build the project in Android Studio and deploy it to the Control Hub or Robot Controller for testing.

> ⚠️ **RI5W Note:** This project is actively being developed. Hardware configuration, subsystem implementations, and OpModes may change throughout the 5-week build.
