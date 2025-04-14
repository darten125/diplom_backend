package com.example.features.userFeatures.currentThesesFeatures


import com.example.database.current_theses.CurrentThesisDTO
import com.example.database.current_theses.CurrentTheses
import com.example.database.processed_requests.ProcessedRequests
import com.example.database.professors.Professors
import com.example.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

private fun mapRole(role: String): String = when(role.lowercase()) {
    "bac" -> "Бакалавриат"
    "mag" -> "Магистратура"
    else -> role
}

class CurrentThesesController(private val call: ApplicationCall) {
    suspend fun create() {
        val receive = call.receive<CreateCurrentThesisReceiveRemote>()
        try {
            val processedRequestUUID = UUID.fromString(receive.processedRequestId)
            val processedRequest = ProcessedRequests.fetchById(processedRequestUUID)

            if (processedRequest == null) {
                call.respond(HttpStatusCode.NotFound, "Запись в processed_requests с указанным id не найдена")
                return
            }

            val studentUUID = processedRequest.studentId
            val professorUUID = processedRequest.professorId
            val title = processedRequest.thesisTitle
            val description = processedRequest.description

            val newThesisId = UUID.randomUUID()
            CurrentTheses.insert(
                CurrentThesisDTO(
                    id = newThesisId,
                    studentId = studentUUID,
                    professorId = professorUUID,
                    title = title,
                    description = description
                )
            )

            Users.updateCurrentThesis(studentUUID, newThesisId)

            ProcessedRequests.deleteByStudent(studentUUID)

            call.respond(
                HttpStatusCode.OK,
                CreateCurrentThesisResponseRemote("Текущая дипломная работа успешно создана")
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Ошибка: ${e.localizedMessage}")
        }
    }

    suspend fun get() {
        val receive = call.receive<GetCurrentThesisReceiveRemote>()
        try {
            val studentUUID = UUID.fromString(receive.studentId)
            val currentThesis = CurrentTheses.fetchByStudent(studentUUID)

            val responseThesis = currentThesis?.let { thesis ->
                val professorDTO = Professors.fetchById(thesis.professorId)
                CurrentThesisResponseRemote(
                    professorName = professorDTO?.name ?: "N/A",
                    professorPosition = professorDTO?.position ?: "N/A",
                    professorDepartment = professorDTO?.department ?: "N/A",
                    title = thesis.title,
                    description = thesis.description
                )
            }

            call.respond(
                HttpStatusCode.OK,
                GetCurrentThesisResponseRemote(thesis = responseThesis)
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Ошибка: ${e.localizedMessage}")
        }
    }

    suspend fun getAllCurrentThesesExcel() {

        val currentThesesList = CurrentTheses.fetchAll()

        val enrichedList = currentThesesList.mapNotNull { thesis ->
            val student = Users.fetchById(thesis.studentId) ?: return@mapNotNull null
            val professor = Professors.fetchById(thesis.professorId) ?: return@mapNotNull null
            Triple(thesis, student, professor)
        }

        val sortedList = enrichedList.sortedWith(compareBy(
            { mapRole(it.second.role).lowercase() },
            { it.second.userGroup.lowercase() }
        ))

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Current Theses")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Student Name")
        header.createCell(1).setCellValue("Group")
        header.createCell(2).setCellValue("Role")
        header.createCell(3).setCellValue("Professor Name")
        header.createCell(4).setCellValue("Professor Position")
        header.createCell(5).setCellValue("Professor Department")
        header.createCell(6).setCellValue("Thesis Title")
        header.createCell(7).setCellValue("Description")

        var rowIndex = 1
        for ((thesis, student, professor) in sortedList) {
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(student.name)
            row.createCell(1).setCellValue(student.userGroup)
            row.createCell(2).setCellValue(mapRole(student.role))
            row.createCell(3).setCellValue(professor.name)
            row.createCell(4).setCellValue(professor.position)
            row.createCell(5).setCellValue(professor.department)
            row.createCell(6).setCellValue(thesis.title)
            row.createCell(7).setCellValue(thesis.description)
        }

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=current_theses.xlsx")
        call.respondBytes(
            bytes = outputStream.toByteArray(),
            contentType = ContentType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        )
    }
}