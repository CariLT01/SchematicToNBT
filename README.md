# Schematic to NBT

Utility Java program for Minecraft structure formats.

This project converts a schematic file (.schem or .schematic for legacy versions) into .NBT structure files mostly intended to be used with the Create mod.

Many programs already exist for converting schematics files to .NBT. However, none of them support splitting (required for Create mod to load your structure) or generating a give list (useful if you don't plan to gather the resources yourself for the build).

## Features

- Automatically split the structure to make sure each .NBT file is below 256 KB
- Automatically generate a give list that contains all the /give commands
- Ability to read .schem and .schematic files

> [!WARNING]
> Experimental side project. May contain bugs and issues. Some large schematics are exported incorrectly.
> Please report any issues in the issues tab.
