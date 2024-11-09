# PacMan in the TorusVerse

A modern take on the classic PacMan game, featuring map-based gameplay with portals and multiple levels.

## Team Members
- Team Leader: Zijun Zhang
- Members: Hogi Kwak, Xiaoyu Pang

## Overview

PacMan in the TorusVerse is an innovative adaptation of the classic PacMan game that combines traditional gameplay elements with new features like portals and multi-level progression. The project includes both a game engine and a map editor, allowing players to both play and create custom levels.

## Features

### Game Features
- **Multiple Levels**: Support for playing multiple maps in ascending order
- **Portal System**: Teleportation mechanics using different types of portal tiles
- **Smart Autoplayer**: Enhanced AI for automated gameplay
- **Classic Elements**: Traditional PacMan mechanics including:
  - Pills and Gold collection
  - Monster avoidance (TX5 and Troll types)
  - Ice cube obstacles

### Map Editor Features
- **Visual Editor**: Intuitive GUI for map creation and editing
- **Tile System**: Multiple tile types including:
  - Walls
  - Pills
  - Gold
  - Ice
  - Portals (White, Yellow, Dark Gold, Dark Gray)
  - Monster spawn points
  - PacMan spawn point
- **Map Validation**: Comprehensive checking system ensuring maps are playable
- **Save/Load**: XML-based map saving and loading functionality

## Technical Details

### Map Requirements
1. Each map must have exactly one PacMan starting point
2. Portal tiles must appear in pairs
3. Maps must contain at least two pills and two gold pieces
4. All pills and gold must be accessible to PacMan

### Game Architecture
- **XML-based Map Storage**: Maps are stored in XML format for easy editing and validation
- **Camera System**: Flexible viewport management for map editing
- **Event-Driven Design**: Utilizes property change listeners for UI updates
- **Modular Structure**: Separate modules for game logic, map editing, and validation

## Getting Started

### Running the Game
```java
java -jar pacman.jar [map_file.xml|map_folder]
```

### Running without arguments: Opens the map editor
- With .xml file: Opens the specified map in edit mode
- With folder path: Loads multiple maps for sequential gameplay

### Map Creation
1. Launch the map editor
2. Select tiles from the palette
3. Click to place tiles on the grid
4. Use the editor tools to test and validate your map
5. Save your map in XML format

## Project Structure
- `src/Game.java`: Main game engine
- `src/mapeditor/`: Map editor components
- `src/utility/`: Utility classes and helpers
- `data/`: Game assets and resources

## Validation System
The project includes two levels of validation:
1. **Level Checking**: Validates individual map files
2. **Game Checking**: Ensures proper organization of multiple map files

## License
The project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contributing
Feel free to fork the project and submit pull requests. For major changes, please open an issue first to discuss what you would like to change.