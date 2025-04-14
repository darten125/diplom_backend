package com.example.features.professorsFeatures.getPreviousTheses
import com.example.database.previous_theses.PreviousTheses
import com.example.database.previous_theses.PreviousThesisDTO
import com.example.database.professors.ProfessorDTO
import com.example.database.professors.Professors
import com.example.features.getPreviousTheses.GetPreviousThesesRequestRemote
import com.example.features.getPreviousTheses.GetPreviousThesesResponseRemote
import com.example.features.getPreviousTheses.PreviousThesisRemote
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import java.util.*


class GetPreviousThesesController(private val call: ApplicationCall) {

    suspend fun getPreviousTheses() {
        val receive = call.receive<GetPreviousThesesRequestRemote>()

        val professorDTO = Professors.fetch(receive.name, receive.position, receive.department)
        if (professorDTO == null) {
            call.respond(HttpStatusCode.NotFound, "Преподаватель не найден")
            return
        }

        val theses = PreviousTheses.fetchByProfessor(professorDTO.id)

        val responseList = theses.map { thesis ->
            PreviousThesisRemote(title = thesis.title)
        }

        call.respond(HttpStatusCode.OK, GetPreviousThesesResponseRemote(theses = responseList))
    }

    suspend fun pushPreviousTheses() {
        val multipart = call.receiveMultipart()
        var fileBytes: ByteArray? = null

        multipart.forEachPart { part ->
            if (part is PartData.FileItem && part.name == "file") {
                fileBytes = part.streamProvider().readBytes()
            }
            part.dispose()
        }
        if (fileBytes == null) {
            call.respond(HttpStatusCode.BadRequest, "Файл не найден")
            return
        }

        val workbook = XSSFWorkbook(ByteArrayInputStream(fileBytes))
        val sheet = workbook.getSheetAt(0)
        val formatter = DataFormatter()

        val headerThesis = "Тема ВКР"
        val headerTeacher = "Фамилия И.О. руководителя ВКР, место работы, должность"

        var thesisColIndex: Int? = null
        var teacherColIndex: Int? = null

        var insertedCount = 0

        for (row in sheet) {
            val cells = row.map { cell -> formatter.formatCellValue(cell).trim() }

            if (cells.all { it.isEmpty() } || cells.size < 2) continue

            if (cells.any { it == headerThesis } && cells.any { it == headerTeacher }) {
                thesisColIndex = cells.indexOfFirst { it == headerThesis }
                teacherColIndex = cells.indexOfFirst { it == headerTeacher }
                continue
            }

            if (thesisColIndex != null && teacherColIndex != null) {
                val thesisTitle = if (thesisColIndex < cells.size) cells[thesisColIndex] else ""
                val teacherInfo = if (teacherColIndex < cells.size) cells[teacherColIndex] else ""

                if (thesisTitle.isEmpty() || teacherInfo.isEmpty() ||
                    thesisTitle == headerThesis || teacherInfo == headerTeacher
                ) {
                    continue
                }

                val parts = teacherInfo.split(",").map { it.trim() }
                if (parts.size < 3) continue

                val teacherName = parts[0]
                val teacherPosition = parts[1]
                val teacherDepartment = parts[2]

                if (teacherName.isEmpty() || teacherPosition.isEmpty() || teacherDepartment.isEmpty()) continue

                val existingProfessor = Professors.fetch(teacherName, teacherPosition, teacherDepartment)
                val professorId = if (existingProfessor != null) {
                    existingProfessor.id
                } else {
                    val newProfessorId = UUID.randomUUID()
                    Professors.insert(
                        ProfessorDTO(
                            id = newProfessorId,
                            name = teacherName,
                            position = teacherPosition,
                            department = teacherDepartment
                        )
                    )
                    newProfessorId
                }

                val newRecordId = UUID.randomUUID()
                PreviousTheses.insert(
                    PreviousThesisDTO(
                        id = newRecordId,
                        professorId = professorId,
                        title = thesisTitle
                    )
                )
                insertedCount++
            }
        }

        workbook.close()
        call.respond(HttpStatusCode.OK, "Вставлено $insertedCount записей в previous_theses")
    }
}