const { z } = require("zod");

/**
 * Validation schemas for student operations
 */

// Student creation schema
const createStudentBodySchema = z.object({
    name: z.string().min(1, "Name is required").max(255),
    email: z.string().email("Invalid email address").max(255),
    phone: z.string().min(1, "Phone is required").max(20),
    gender: z.string().min(1, "Gender is required"),
    dob: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Date must be in YYYY-MM-DD format"),
    class: z.string().min(1, "Class is required").max(100),
    section: z.string().max(100).optional(),
    roll: z.string().min(1, "Roll is required").max(50),
    fatherName: z.string().min(1, "Father name is required").max(255),
    fatherPhone: z.string().max(20).optional(),
    motherName: z.string().max(255).optional(),
    motherPhone: z.string().max(20).optional(),
    guardianName: z.string().min(1, "Guardian name is required").max(255),
    guardianPhone: z.string().min(1, "Guardian phone is required").max(20),
    relationOfGuardian: z.string().min(1, "Relation of guardian is required").max(100),
    currentAddress: z.string().min(1, "Current address is required").max(500),
    permanentAddress: z.string().min(1, "Permanent address is required").max(500),
    admissionDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Date must be in YYYY-MM-DD format"),
    systemAccess: z.boolean().optional(),
});

// Student creation schema with body wrapper
const createStudentSchema = z.object({
    body: createStudentBodySchema,
});

// Student update schema (all fields optional except at least one must be provided)
const updateStudentBodySchema = z.object({
    name: z.string().min(1).max(255).optional(),
    email: z.string().email().max(255).optional(),
    phone: z.string().max(20).optional(),
    gender: z.string().optional(),
    dob: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional(),
    class: z.string().max(100).optional(),
    section: z.string().max(100).optional(),
    roll: z.string().max(50).optional(),
    fatherName: z.string().max(255).optional(),
    fatherPhone: z.string().max(20).optional(),
    motherName: z.string().max(255).optional(),
    motherPhone: z.string().max(20).optional(),
    guardianName: z.string().max(255).optional(),
    guardianPhone: z.string().max(20).optional(),
    relationOfGuardian: z.string().max(100).optional(),
    currentAddress: z.string().max(500).optional(),
    permanentAddress: z.string().max(500).optional(),
    admissionDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional(),
    systemAccess: z.boolean().optional(),
}).refine(data => Object.keys(data).length > 0, {
    message: "At least one field must be provided for update",
});

// Student update schema with body and params wrapper
const updateStudentSchema = z.object({
    body: updateStudentBodySchema,
    params: z.object({
        id: z.string().min(1, "Student ID is required"),
    }),
});

// Query parameters schema for getting all students
const getAllStudentsQuerySchema = z.object({
    name: z.string().optional(),
    className: z.string().optional(), // Backend uses className in query
    section: z.string().optional(),
    roll: z.string().optional(),
});

// Get all students schema with query wrapper
const getAllStudentsSchema = z.object({
    query: getAllStudentsQuerySchema,
});

// Get student detail schema with params wrapper
const getStudentDetailSchema = z.object({
    params: z.object({
        id: z.string().min(1, "Student ID is required"),
    }),
});

// Student status update schema
const studentStatusBodySchema = z.object({
    status: z.boolean({
        required_error: "Status is required",
        invalid_type_error: "Status must be a boolean",
    }),
});

// Student status update schema with body and params wrapper
const studentStatusSchema = z.object({
    body: studentStatusBodySchema,
    params: z.object({
        id: z.string().min(1, "Student ID is required"),
    }),
});

module.exports = {
    createStudentSchema,
    updateStudentSchema,
    getAllStudentsSchema,
    getStudentDetailSchema,
    studentStatusSchema,
};

