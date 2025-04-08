package com.example.features.ProcessedRequestsFeatures

import com.example.database.pending_supervision_requests.PendingSupervisionRequests
import com.example.database.processed_requests.ProcessedRequestDTO
import com.example.database.processed_requests.ProcessedRequests
import com.example.database.professors.Professors
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import java.util.*

class ProcessedRequestsController(private val call: ApplicationCall) {

    suspend fun pushProcessedRequests() {
        // Принимаем файл через multipart
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
        val headerRow = sheet.getRow(0)
        if (headerRow == null) {
            workbook.close()
            call.respond(HttpStatusCode.BadRequest, "Пустой файл")
            return
        }

        // Ожидаемые заголовки (в точном порядке, теперь 9 столбцов)
        val expectedHeaders = listOf(
            "ID",
            "Student Name",
            "Group",
            "Professor Name",
            "Professor Position",
            "Professor Department",
            "Thesis Title",
            "Description",
            "Accepted"
        )

        val formatter = DataFormatter()
        // Проверяем заголовки
        for (i in expectedHeaders.indices) {
            val cell = headerRow.getCell(i)
            if (cell == null || cell.stringCellValue.trim() != expectedHeaders[i]) {
                workbook.close()
                call.respond(HttpStatusCode.BadRequest, "Неверный формат заголовков в файле")
                return
            }
        }

        var processedCount = 0
        // Обрабатываем каждую строку, начиная со второй (индекс 1)
        for (rowIndex in 1 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(rowIndex) ?: continue

            // Проверяем, что в строке ровно 9 ячеек
            if (row.physicalNumberOfCells != 9) continue

            // Собираем значения ячеек
            val cellValues = (0 until 9).map { index ->
                val cell = row.getCell(index)
                formatter.formatCellValue(cell).trim()
            }

            // Если хоть одно поле пустое, пропускаем строку
            if (cellValues.any { it.isEmpty() }) continue

            // Проверяем столбец Accepted (последний столбец)
            val acceptedStr = cellValues[8]
            if (acceptedStr != "0" && acceptedStr != "1") continue
            val acceptedBoolean = acceptedStr == "1"

            // Парсим id записи из первой ячейки
            val pendingId = try {
                UUID.fromString(cellValues[0])
            } catch (e: Exception) {
                continue
            }

            // Получаем запись из pending_supervision_requests по id
            val pendingRequest = PendingSupervisionRequests.fetchById(pendingId) ?: continue

            // Создаем новую запись в processed_requests с новым UUID
            val newProcessedId = UUID.randomUUID()
            ProcessedRequests.insert(
                ProcessedRequestDTO(
                    id = newProcessedId,
                    studentId = pendingRequest.studentId,
                    professorId = pendingRequest.professorId,
                    thesisTitle = pendingRequest.thesisTitle,
                    description = pendingRequest.description,
                    accepted = acceptedBoolean
                )
            )

            // Удаляем запись из pending_supervision_requests
            PendingSupervisionRequests.deleteById(pendingId)
            processedCount++
        }
        workbook.close()
        call.respond(HttpStatusCode.OK, "Обработано $processedCount записей")
    }

    suspend fun getUserProcessedRequests() {
        // Получаем тело запроса, содержащее id студента (в виде строки)
        val request = call.receive<GetProcessedRequestsForUserRequest>()
        val studentUUID = try {
            UUID.fromString(request.studentId)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Неверный формат идентификатора студента")
            return
        }

        // Получаем список обработанных запросов по studentId
        val processedList = ProcessedRequests.fetchByStudent(studentUUID)

        // Преобразуем в формат ответа. Вместо professorId получаем имя, должность и кафедру преподавателя.
        val responseList = processedList.map { processed ->
            // Получаем данные преподавателя по professorId
            val professorDTO = Professors.fetchById(processed.professorId)
            val professorName = professorDTO?.name ?: processed.professorId.toString()
            val professorPosition = professorDTO?.position ?: "N/A"
            val professorDepartment = professorDTO?.department ?: "N/A"

            ProcessedRequestResponse(
                id = processed.id.toString(),
                professorName = professorName,
                professorPosition = professorPosition,
                professorDepartment = professorDepartment,
                thesisTitle = processed.thesisTitle,
                description = processed.description,
                accepted = processed.accepted
            )
        }
        call.respond(HttpStatusCode.OK, GetProcessedRequestsForUserResponse(requests = responseList))
    }
}