# Text Editor - Java Swing Application

## Overview
This is a Java Swing-based text editor that allows users to create, open, edit, and save text files. It includes a menu bar with essential actions, auto-save functionality, and a preferences window for customization.

It optimises it-self to what OS you are running, macOS or other. All are compatible. It makes use of macOS menu bar if using macOS and uses CMD for shortcuts rather than CTRL.

The preferences are saved using properties.

## Features

### Basic Text Editing
- **New File**: Opens a new text editor window.
- **Open File**: Selects a text file from the system and opens it in the editor.
- **Save File**: Saves changes to the currently opened file.
- **Save As**: Saves the file to a new location.

### Edit Options
- Cut, Copy, and Paste functionalities.

### Auto-Save Feature
- Automatically saves changes every 10 seconds if auto-save is enabled.

### Preferences Window
- Allows customization of font size, font family, text color, and background color.
- Enables or disables auto-save.
- Saves and restores settings when reopening.

### MacOS Integration
- Uses ⌘ (Command key) shortcuts instead of Ctrl for Mac users.
- Moves the menu bar to the system menu bar on macOS.

## Usage

### Running the Application
Compile and run the Java file:
```sh
javac TextEditorFrame.java
java TextEditorFrame
```
or run the main method in an IDE.
```java
public static void main(String[] args) {
    new TextEditorFrame(null);
}
```

