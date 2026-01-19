# Bookmap Jigsaw DOM Plugin

This is a Bookmap Layer 1 API plugin that implements a DOM (Depth of Market) visualization, similar to Jigsaw trading tools.

## Prerequisites

1.  **Java 17 SDK**: Ensure Java 17 is installed and available.
2.  **Bookmap Application**: Bookmap must be installed to provide the necessary API libraries (`velox.api.*`).
    *   Default expected path: `C:/Program Files/Bookmap/lib`
    *   If your Bookmap installation is elsewhere, update the path in `build.gradle`:
        ```gradle
        dependencies {
            compileOnly fileTree(dir: 'path/to/your/Bookmap/lib', include: ['*.jar'])
        }
        ```

## Building the Project

To build the plugin JAR, run the following command in the project root:

- **Windows:**
  ```powershell
  ./gradlew build
  ```

- **Linux/Mac:**
  ```bash
  ./gradlew build
  ```

If the build is successful, the plugin JAR will be generated in `build/libs/qtdom-1.0-SNAPSHOT.jar`.

## Running the Plugin

Since this is a plugin, it must be loaded into Bookmap:

1.  Open **Bookmap**.
2.  Go to **Settings** > **API Plugins**.
3.  Click **Add** (or similar "Load" button).
4.  Navigate to the `build/libs` directory of this project.
5.  Select `qtdom-1.0-SNAPSHOT.jar` and click **Open**.
6.  The "Jigsaw DOM" checkbox should appear in the plugins list. Enable it to activate the module.

## Troubleshooting

- **Build Fails with "package velox.api... does not exist"**:
  This means the Bookmap libraries were not found. Verify that the path in `build.gradle` matches your actual Bookmap installation directory.

- **"Hello and welcome!" when running Main.java**:
  The `src/main/java/com/shashin/bookmap/Main.java` file is a placeholder template and does not run the plugin. Please follow the "Running the Plugin" steps above.
