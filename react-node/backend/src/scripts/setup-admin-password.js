const { db } = require("../config");
const { generateHashedPassword } = require("../utils");

const setupAdminPassword = async () => {
    try {
        // Check if admin password is already set
        const checkQuery = "SELECT password FROM users WHERE email = 'admin@school-admin.com'";
        const checkResult = await db.query(checkQuery);
        
        if (checkResult.rows.length === 0) {
            console.log("Admin user not found. Skipping password setup.");
            return;
        }
        
        // If password is already set and not a placeholder, skip
        const existingPassword = checkResult.rows[0].password;
        if (existingPassword && !existingPassword.includes('placeholder')) {
            console.log("Admin password already configured. Skipping setup.");
            return;
        }
        
        console.log("Setting up admin password...");
        
        // Generate password hash
        const password = "3OU4zn3q6Zh9";
        const hashedPassword = await generateHashedPassword(password);
        
        // Update admin user
        const updateQuery = `
            UPDATE users 
            SET password = $1, is_active = true, is_email_verified = true
            WHERE email = 'admin@school-admin.com'
            RETURNING id, name, email
        `;
        
        const result = await db.query(updateQuery, [hashedPassword]);
        
        if (result.rows.length > 0) {
            console.log("✓ Admin password set successfully!");
            console.log(`  Admin user: ${result.rows[0].email}`);
        }
    } catch (error) {
        // Don't fail the container startup if password setup fails
        console.error("Warning: Could not set admin password:", error.message);
        console.error("You may need to set it manually or through the password setup flow.");
    }
};

setupAdminPassword();

