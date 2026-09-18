# 👁️ Vision — Limelight & Localization

This package contains the robot's **Limelight vision system**, AprilTag detection, and vision-based localization.

For **Robot In 5 Weeks (RI5W)**, the vision system is intentionally kept simple. The goal is to have reliable localization without adding unnecessary complexity while the rest of the robot is still being developed.

---

## 🧠 RI5W Vision Strategy

For RI5W, we are reusing the **sensor-fusion system from DECODE**.

The original DECODE system used the Limelight alongside **Pinpoint localization** to correct the robot's estimated position when a reliable AprilTag sighting was available.

For RI5W, we are bringing back that general approach rather than immediately implementing a Kalman filter.

### Why Not Kalman Yet?

A Kalman filter is useful when you have frequent, noisy measurements that can continuously improve an estimate.

That is not really the situation with this season's field setup.

The AprilTags are located on the **underside of the HIVE's CELLS**, meaning the Limelight cannot simply look across the field and continuously see AprilTags. Only the currently downward-facing CELL is visible at a given time.

Because of this, vision measurements are expected to be:

* **Near-field**
* **Intermittent**
* **Dependent on the HIVE's current position**
* **Available only when the Limelight has a usable view of a tag**

For RI5W, the simpler DECODE-style approach is therefore preferred.

> **Main idea:** Let Pinpoint handle normal localization, and use the Limelight as an occasional correction when a reliable AprilTag measurement is available.

---

## 📷 Limelight Placement

The Limelight is most likley going to be mounted faced UPWARD.

This is different from a traditional Limelight setup where the camera can look across the field and search for AprilTags.

For this season, the relevant AprilTags are attached to the **bottom of the HIVE's CELLS**, so the Limelight needs to look upward when the robot is underneath the HIVE.

This makes the Limelight a **situational vision sensor rather than a constantly-visible field reference**.

Because the mounting geometry is different from DECODE, the camera mounting measurements and offsets must be calibrated for the new robot before trusting distance or pose calculations.

---

## 🏷️ AprilTag Detection

The current AprilTag groups for BIOBUZZ are:

| Alliance / Side          | Tag IDs |
| ------------------------ | ------- |
| **Red — Scoring Side**   | 30–33   |
| **Red — Audience Side**  | 34–37   |
| **Blue — Audience Side** | 38–41   |
| **Blue — Scoring Side**  | 42–45   |

These IDs represent the different field-side tag groups. They should **not** be treated as simply "up" versus "down" tags because the HIVE can be tipped in different directions.

The Limelight code supports checking individual tags or checking whether a tag belongs to one of these ranges.

---

## 🔭 Fiducial 9

The **Fiducial 9 / artifact-detection pipeline is probably not required for RI5W**.

The current plan is to focus on the AprilTag-based localization system first.

Fiducial 9 may become useful later if we find a practical way to use vision for **artifact detection**, but there is no reason to build the RI5W localization system around it right now.

---

## 📍 Localization

The vision system uses **MT1** for occasional Limelight-based localization.

The current RI5W approach is:

```text
Pinpoint
   ↓
Normal robot localization
   ↓
Limelight sees a usable AprilTag
   ↓
MT1 provides a vision position
   ↓
Use the vision measurement as a correction
```

This keeps vision from becoming a required part of normal driving.

If the Limelight cannot see a valid tag, the robot can continue using Pinpoint.

---

## 🧪 Current Development Status

### ⚠️ Limelight is currently not fully operational

As of **September 12, 2026**, Kickoff has just ended and the Limelight system is still being adapted to the new game.

The camera needs to be **recalibrated for its new mounting position**, and several other robot systems that the vision code will eventually interact with have not been completed yet.

Because of this, **AprilTag position snapping is not currently expected to work**.

This does **not** mean the vision code is abandoned or fundamentally broken. The code is being prepared around the new game setup while the required hardware and configuration are still being developed.

---

## 🔧 DECODE Code Reuse

The RI5W vision system intentionally reuses parts of the proven **DECODE Limelight implementation**.

The existing MT1 pose transformation and snapshot helpers are being retained because those transformations were already tested on-robot during DECODE.

However, the following values **must be recalibrated for the new robot**:

* Camera height
* Tag height
* Camera tilt
* Camera mounting offset
* Distance offsets
* Limelight field map configuration

Do **not** assume the old DECODE mounting measurements are correct for RI5W.

---

## 🚀 Future Competition Bot

RI5W is being used to establish the basic vision architecture without overcomplicating the system.

For the main competition robot, the sensor-fusion system may be expanded if testing shows that it is necessary.

Possible future improvements include:

* More advanced sensor fusion
* Kalman filtering
* Better handling of intermittent vision measurements
* Artifact detection
* Additional vision-assisted localization

These are **future possibilities, not requirements for RI5W**.

### Current Priority

> **Get reliable localization working first. Add complexity only when the robot actually needs it.**
