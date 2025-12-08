const express = require("express");
const router = express.Router();
const { validateRequest } = require("../../utils");
const studentController = require("./students-controller");
const {
    createStudentSchema,
    updateStudentSchema,
    getAllStudentsSchema,
    getStudentDetailSchema,
    studentStatusSchema,
} = require("./students-validator");

router.get("", validateRequest(getAllStudentsSchema), studentController.handleGetAllStudents);
router.post("", validateRequest(createStudentSchema), studentController.handleAddStudent);
router.get("/:id", validateRequest(getStudentDetailSchema), studentController.handleGetStudentDetail);
router.post("/:id/status", validateRequest(studentStatusSchema), studentController.handleStudentStatus);
router.put("/:id", validateRequest(updateStudentSchema), studentController.handleUpdateStudent);

module.exports = { studentsRoutes: router };
