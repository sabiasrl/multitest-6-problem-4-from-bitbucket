// Mock dependencies first
jest.mock("../students-repository");
jest.mock("../../../shared/repository");
jest.mock("../../../utils", () => {
    const actualUtils = jest.requireActual("../../../utils");
    return {
        ...actualUtils,
        sendAccountVerificationEmail: jest.fn(),
    };
});

const {
    getAllStudents,
    getStudentDetail,
    addNewStudent,
    updateStudent,
    setStudentStatus,
} = require("../students-service");
const { findAllStudents, findStudentDetail, addOrUpdateStudent, findStudentToSetStatus } = require("../students-repository");
const { findUserById } = require("../../../shared/repository");
const { ApiError, sendAccountVerificationEmail } = require("../../../utils");

describe("Students Service", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe("getAllStudents", () => {
        it("should return all students successfully", async () => {
            const mockPayload = { name: "John", className: "10" };
            const mockStudents = [
                { id: 1, name: "John Doe", email: "john@example.com" },
                { id: 2, name: "Jane Smith", email: "jane@example.com" },
            ];
            findAllStudents.mockResolvedValue(mockStudents);

            const result = await getAllStudents(mockPayload);

            expect(findAllStudents).toHaveBeenCalledWith(mockPayload);
            expect(result).toEqual(mockStudents);
        });

        it("should throw ApiError when no students found", async () => {
            const mockPayload = { name: "NonExistent" };
            findAllStudents.mockResolvedValue([]);

            await expect(getAllStudents(mockPayload)).rejects.toThrow(ApiError);
            await expect(getAllStudents(mockPayload)).rejects.toThrow("Students not found");
        });
    });

    describe("getStudentDetail", () => {
        it("should return student detail successfully", async () => {
            const studentId = "1";
            const mockStudent = {
                id: 1,
                name: "John Doe",
                email: "john@example.com",
                class: "10",
                section: "A",
            };
            findUserById.mockResolvedValue({ id: 1 });
            findStudentDetail.mockResolvedValue(mockStudent);

            const result = await getStudentDetail(studentId);

            expect(findUserById).toHaveBeenCalledWith(studentId);
            expect(findStudentDetail).toHaveBeenCalledWith(studentId);
            expect(result).toEqual(mockStudent);
        });

        it("should throw ApiError when student not found", async () => {
            const studentId = "999";
            findUserById.mockResolvedValue(null);

            await expect(getStudentDetail(studentId)).rejects.toThrow(ApiError);
            await expect(getStudentDetail(studentId)).rejects.toThrow("Student not found");
        });

        it("should throw ApiError when student detail is null", async () => {
            const studentId = "1";
            findUserById.mockResolvedValue({ id: 1 });
            findStudentDetail.mockResolvedValue(null);

            await expect(getStudentDetail(studentId)).rejects.toThrow(ApiError);
            await expect(getStudentDetail(studentId)).rejects.toThrow("Student not found");
        });
    });

    describe("addNewStudent", () => {
        it("should add student and send verification email successfully", async () => {
            const mockPayload = {
                name: "John Doe",
                email: "john@example.com",
                phone: "1234567890",
            };
            const mockResult = {
                status: true,
                userId: "1",
                message: "Student added successfully",
            };
            addOrUpdateStudent.mockResolvedValue(mockResult);
            sendAccountVerificationEmail.mockResolvedValue(true);

            const result = await addNewStudent(mockPayload);

            expect(addOrUpdateStudent).toHaveBeenCalledWith(mockPayload);
            expect(sendAccountVerificationEmail).toHaveBeenCalledWith({
                userId: "1",
                userEmail: mockPayload.email,
            });
            expect(result.message).toBe("Student added and verification email sent successfully.");
        });

        it("should add student but handle email send failure", async () => {
            const mockPayload = {
                name: "John Doe",
                email: "john@example.com",
            };
            const mockResult = {
                status: true,
                userId: "1",
                message: "Student added successfully",
            };
            addOrUpdateStudent.mockResolvedValue(mockResult);
            sendAccountVerificationEmail.mockRejectedValue(new Error("Email send failed"));

            const result = await addNewStudent(mockPayload);

            expect(result.message).toBe("Student added, but failed to send verification email.");
        });

        it("should throw ApiError when add operation fails", async () => {
            const mockPayload = { name: "John Doe", email: "john@example.com" };
            const mockResult = {
                status: false,
                message: "Database error",
            };
            addOrUpdateStudent.mockResolvedValue(mockResult);

            await expect(addNewStudent(mockPayload)).rejects.toThrow(ApiError);
            await expect(addNewStudent(mockPayload)).rejects.toThrow("Database error");
        });

        it("should throw ApiError when unable to add student", async () => {
            const mockPayload = { name: "John Doe", email: "john@example.com" };
            addOrUpdateStudent.mockRejectedValue(new Error("Database connection failed"));

            await expect(addNewStudent(mockPayload)).rejects.toThrow(ApiError);
            await expect(addNewStudent(mockPayload)).rejects.toThrow("Unable to add student");
        });
    });

    describe("updateStudent", () => {
        it("should update student successfully", async () => {
            const mockPayload = {
                userId: "1",
                name: "John Updated",
                email: "john.updated@example.com",
            };
            const mockResult = {
                status: true,
                message: "Student updated successfully",
            };
            addOrUpdateStudent.mockResolvedValue(mockResult);

            const result = await updateStudent(mockPayload);

            expect(addOrUpdateStudent).toHaveBeenCalledWith(mockPayload);
            expect(result.message).toBe("Student updated successfully");
        });

        it("should throw ApiError when update operation fails", async () => {
            const mockPayload = { userId: "1", name: "John Updated" };
            const mockResult = {
                status: false,
                message: "Update failed",
            };
            addOrUpdateStudent.mockResolvedValue(mockResult);

            await expect(updateStudent(mockPayload)).rejects.toThrow(ApiError);
            await expect(updateStudent(mockPayload)).rejects.toThrow("Update failed");
        });
    });

    describe("setStudentStatus", () => {
        it("should update student status successfully", async () => {
            const mockPayload = {
                userId: "1",
                reviewerId: "2",
                status: true,
            };
            findUserById.mockResolvedValue({ id: 1 });
            findStudentToSetStatus.mockResolvedValue(1);

            const result = await setStudentStatus(mockPayload);

            expect(findUserById).toHaveBeenCalledWith("1");
            expect(findStudentToSetStatus).toHaveBeenCalledWith(mockPayload);
            expect(result.message).toBe("Student status changed successfully");
        });

        it("should throw ApiError when student not found", async () => {
            const mockPayload = {
                userId: "999",
                reviewerId: "2",
                status: true,
            };
            findUserById.mockResolvedValue(null);

            await expect(setStudentStatus(mockPayload)).rejects.toThrow(ApiError);
            await expect(setStudentStatus(mockPayload)).rejects.toThrow("Student not found");
        });

        it("should throw ApiError when status update fails", async () => {
            const mockPayload = {
                userId: "1",
                reviewerId: "2",
                status: true,
            };
            findUserById.mockResolvedValue({ id: 1 });
            findStudentToSetStatus.mockResolvedValue(0);

            await expect(setStudentStatus(mockPayload)).rejects.toThrow(ApiError);
            await expect(setStudentStatus(mockPayload)).rejects.toThrow("Unable to disable student");
        });
    });
});

