package com.example.features.PendingSupervisionRequestsFeatures

import com.example.database.pending_supervision_requests.PendingSupervisionRequestDTO
import com.example.database.pending_supervision_requests.PendingSupervisionRequests
import com.example.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.*
import com.example.database.professors.Professors
import java.io.ByteArrayOutputStream

class PendingSupervisionRequestController(private val call: ApplicationCall) {

    // Создание новой записи
    suspend fun createPendingRequest() {
        val request = call.receive<CreatePendingRequestRemote>()
        val dto = PendingSupervisionRequestDTO(
            id = UUID.randomUUID(),
            studentId = UUID.fromString(request.studentId),
            professorId = UUID.fromString(request.professorId),
            thesisTitle = request.thesisTitle,
            description = request.description,
            accepted = null // по умолчанию запрос ещё не обработан
        )
        PendingSupervisionRequests.insert(dto)
        call.respond(HttpStatusCode.OK, "Запрос успешно создан")
    }

    // Экспорт данных в Excel
    suspend fun getPendingRequestsList() {
        val list = PendingSupervisionRequests.fetchAll()

        val enrichedRequests = list.map { req ->
            val studentName = Users.getNameById(req.studentId) ?: req.studentId.toString()
            val studentGroup = Users.getUserGroupById(req.studentId) ?: "N/A"
            Triple(req, studentName, studentGroup)
        }.sortedBy { it.third }  // сортировка по группе

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Pending Supervision Requests")

        // Заголовки столбцов: добавлены колонки для должности и кафедры преподавателя
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("ID")
        header.createCell(1).setCellValue("Student Name")
        header.createCell(2).setCellValue("Group")
        header.createCell(3).setCellValue("Professor Name")
        header.createCell(4).setCellValue("Professor Position")
        header.createCell(5).setCellValue("Professor Department")
        header.createCell(6).setCellValue("Thesis Title")
        header.createCell(7).setCellValue("Description")
        header.createCell(8).setCellValue("Accepted")

        var rowIndex = 1
        for ((req, studentName, studentGroup) in enrichedRequests) {

            // Получаем данные преподавателя через метод fetchById
            val professorDTO = Professors.fetchById(req.professorId)
            val professorName = professorDTO?.name ?: req.professorId.toString()
            val professorPosition = professorDTO?.position ?: "N/A"
            val professorDepartment = professorDTO?.department ?: "N/A"

            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(req.id.toString())
            row.createCell(1).setCellValue(studentName)
            row.createCell(2).setCellValue(studentGroup)
            row.createCell(3).setCellValue(professorName)
            row.createCell(4).setCellValue(professorPosition)
            row.createCell(5).setCellValue(professorDepartment)
            row.createCell(6).setCellValue(req.thesisTitle)
            row.createCell(7).setCellValue(req.description)
            row.createCell(8).setCellValue(req.accepted?.toString() ?: "null")
        }

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        call.response.header(
            HttpHeaders.ContentDisposition,
            "attachment; filename=pending_supervision_requests.xlsx"
        )
        call.respondBytes(
            bytes = outputStream.toByteArray(),
            contentType = ContentType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        )
    }
}