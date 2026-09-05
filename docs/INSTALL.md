# Install Pete's Reps on Android

This page is for a normal person installing Pete's Reps on an Android phone. You do **not** need Android Studio, ADB, Gradle, a USB cable, or a computer.

Pete's Reps is currently an alpha build distributed from GitHub Actions. It does not require a Pete's Reps account, subscription, analytics service, or cloud connection to run.

## Install it on your phone

1. On the Android phone, open a web browser and sign in to GitHub.
2. Open the **Pete's Reps** repository.
3. Tap **Actions** near the top of the repository page.
4. Open the newest **Android CI** run on `main` that has a green check mark.
5. Scroll down to the **Artifacts** section.
6. Tap **`petes-reps-debug-apk`**.
7. GitHub downloads a ZIP file to the phone.
8. Open the phone's **Files** app and go to **Downloads**.
9. Tap the downloaded ZIP and choose **Extract** if the phone does not extract it automatically.
10. Open the extracted folder.
11. Tap **`app-debug.apk`**.
12. If Android says the browser or Files app is not allowed to install unknown apps, tap **Settings**, enable **Allow from this source** for the app you intentionally used to open this Pete's Reps APK, then return to the installer.
13. Tap **Install**.
14. Tap **Open**, or launch **Pete's Reps** from the app drawer.

You are done. There is nothing to compile or build yourself.

## What you should see

The first screen shows:

- **Pete's Reps** at the top
- **Session 1** if this is a new installation
- four movements for today's session
- a **Swap** button on each movement
- a large **Start 25:00 session** button
- **Training history** below the session

If your screen looks broadly like that, the app is installed correctly.

## Start the first workout

1. Read the four movements before starting.
2. If a movement is not workable today, tap **Swap**. Pete's Reps replaces it without asking you to explain why.
3. Tap **Start 25:00 session** when you are ready to train.
4. The master 25-minute clock starts immediately.
5. Follow the movement currently shown.
6. Record the objective result requested by the app: reps or seconds.
7. Tap **Next** to continue. If the current movement timer expires first, the app says **MOVE ON**.
8. You can tap **Swap** during the workout too. Swapping does not reset either timer.
9. You can open **View full session** without pausing the clock.
10. If you reach the last movement early, tap **Complete session**. Otherwise the session automatically stops and saves when the master clock reaches **00:00**.

The 25 minutes include training, transitions, logging, mobility, stretching, looking at the overview, and time spent swapping a movement.

## Before uninstalling or replacing the app

Training history is stored locally on the phone.

Before uninstalling Pete's Reps, replacing the phone, or doing anything that might require a fresh install:

1. Open Pete's Reps while no workout is running.
2. Scroll to **Training history**.
3. Tap **Export training backup**.
4. Save the `.preps` backup somewhere outside the app.

To recover it later, install Pete's Reps, tap **Restore training backup**, select the backup, review the confirmation, and restore it.

See **[BACKUP.md](BACKUP.md)** for the full backup contract.

## Updating the current alpha

The current GitHub Actions download is a **debug APK**, not the final stable release channel.

A future build may install directly over the old one, but debug signing can sometimes differ between environments. Do **not** uninstall the current app merely to make an update install unless you have first exported a training backup, because uninstalling Android apps normally removes their private local data.

The repository already contains the future stable signing and GitHub Release pipeline. Once the durable signing key is provisioned, consistently signed GitHub Releases will become the normal update path.

See **[RELEASE.md](RELEASE.md)** for the release-signing plan.

## Core equipment

Pete's Reps currently assumes access to:

- a securely installed pull-up bar
- a kettlebell
- a medicine ball

Conventional weights are optional rather than required.

## For developers

If you actually want to build Pete's Reps from source, run Gradle, use ADB, or inspect the Android build configuration, those instructions are intentionally kept separate in **[DEVELOPING.md](DEVELOPING.md)**.

## Privacy

Pete's Reps is local-first. The core training flow does not require an account, analytics service, advertising service, subscription, or permanent network connection. Workout history lives on the device.

## Training note

Use securely installed equipment and an appropriate movement space. Stop a movement that produces sharp pain, dizziness, or another concerning symptom. Pete's Reps is training software, not medical care.
