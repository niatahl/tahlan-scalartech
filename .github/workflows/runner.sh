#!/bin/sh

### Edit
# Your mod name. The version number will be attached to this to form "My-Mod-1.0.0"
MOD_FOLDER_NAME=scalartech-solutions
echo "Folder name without version will be $MOD_FOLDER_NAME"
###


chmod +x ./zipMod.sh
sh ./zipMod.sh "./../.." "$MOD_FOLDER_NAME"