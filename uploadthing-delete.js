#!/usr/bin/env node

/**
 * UploadThing File Deletion Script
 * 
 * This script uses UploadThing's official JavaScript SDK to delete files.
 * It's designed to be called from Java backend applications.
 * 
 * Usage: node uploadthing-delete.js <fileKey1> [fileKey2] [fileKey3] ...
 * 
 * Environment Variables Required:
 * - UPLOADTHING_TOKEN: Your UploadThing token
 */

const { UTApi } = require('uploadthing/server');

// Get command line arguments (file keys to delete)
const fileKeys = process.argv.slice(2);

if (fileKeys.length === 0) {
    console.error('Error: No file keys provided');
    console.log('Usage: node uploadthing-delete.js <fileKey1> [fileKey2] [fileKey3] ...');
    process.exit(1);
}

// Get environment variables
const token = process.env.UPLOADTHING_TOKEN;

if (!token) {
    console.error('Error: Missing required environment variable');
    console.log('Required: UPLOADTHING_TOKEN');
    process.exit(1);
}

// Initialize UploadThing API with token
const utapi = new UTApi({ token });

async function deleteFiles() {
    try {
        console.log(`Attempting to delete ${fileKeys.length} file(s): ${fileKeys.join(', ')}`);
        
        // Delete files using UploadThing SDK
        const result = await utapi.deleteFiles(fileKeys);
        
        // Output result as JSON for Java to parse
        console.log(JSON.stringify({
            success: true,
            message: `Successfully deleted ${fileKeys.length} file(s)`,
            deletedFiles: fileKeys,
            result: result
        }));
        
    } catch (error) {
        console.error(JSON.stringify({
            success: false,
            message: `Failed to delete files: ${error.message}`,
            error: error.toString(),
            fileKeys: fileKeys
        }));
        process.exit(1);
    }
}

// Run the deletion
deleteFiles();
