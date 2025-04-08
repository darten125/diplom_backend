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
        // Получаем данные из запроса
        val receive = call.receive<GetPreviousThesesRequestRemote>()

        // Поиск преподавателя по имени, должности и кафедре
        val professorDTO = Professors.fetch(receive.name, receive.position, receive.department)
        if (professorDTO == null) {
            call.respond(HttpStatusCode.NotFound, "Преподаватель не найден")
            return
        }

        // Получаем список предыдущих работ для найденного преподавателя
        val theses = PreviousTheses.fetchByProfessor(professorDTO.id)

        // Преобразуем результат: оставляем только заголовки
        val responseList = theses.map { thesis ->
            PreviousThesisRemote(title = thesis.title)
        }

        call.respond(HttpStatusCode.OK, GetPreviousThesesResponseRemote(theses = responseList))
    }

    suspend fun pushPreviousTheses() {
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
        val formatter = DataFormatter()

        // Ключевые строки заголовков
        val headerThesis = "Тема ВКР"
        val headerTeacher = "Фамилия И.О. руководителя ВКР, место работы, должность"

        // Будем искать заголовок в каждой строке. Когда они найдены, сохраняем индексы.
        var thesisColIndex: Int? = null
        var teacherColIndex: Int? = null

        var insertedCount = 0

        // Итерация по всем строкам листа.
        for (row in sheet) {
            // Считываем все ячейки строки как строки с обрезкой пробелов.
            val cells = row.map { cell -> formatter.formatCellValue(cell).trim() }

            // Если строка пустая или содержит мало данных, пропускаем
            if (cells.all { it.isEmpty() } || cells.size < 2) continue

            // Проверяем, является ли эта строка заголовочной
            if (cells.any { it == headerThesis } && cells.any { it == headerTeacher }) {
                // Определяем индексы, где встречаются нужные заголовки.
                thesisColIndex = cells.indexOfFirst { it == headerThesis }
                teacherColIndex = cells.indexOfFirst { it == headerTeacher }
                // Заголовочную строку не обрабатываем как данные
                continue
            }

            // Если мы уже нашли заголовок (индексы не null) – пытаемся обработать строку как данные.
            if (thesisColIndex != null && teacherColIndex != null) {
                // Получаем значение для темы ВКР и для данных о преподавателе.
                val thesisTitle = if (thesisColIndex < cells.size) cells[thesisColIndex] else ""
                val teacherInfo = if (teacherColIndex < cells.size) cells[teacherColIndex] else ""

                // Если хотя бы одно из значений пустое или совпадает с заголовком, пропускаем строку.
                if (thesisTitle.isEmpty() || teacherInfo.isEmpty() ||
                    thesisTitle == headerThesis || teacherInfo == headerTeacher
                ) {
                    continue
                }

                // Разбиваем строку с данными о преподавателе по запятым и обрезаем пробелы.
                val parts = teacherInfo.split(",").map { it.trim() }
                // Ожидаем, что частей будет минимум 4:
                // parts[0] – ФИО руководителя,
                // parts[1] – должность,
                // parts[2] – кафедра.
                if (parts.size < 3) continue

                val teacherName = parts[0]
                val teacherPosition = parts[1]
                val teacherDepartment = parts[2]

                // Если какая-либо из ключевых частей отсутствует – пропускаем строку.
                if (teacherName.isEmpty() || teacherPosition.isEmpty() || teacherDepartment.isEmpty()) continue

                // Все данные валидны – ищем преподавателя.
                val existingProfessor = Professors.fetch(teacherName, teacherPosition, teacherDepartment)
                val professorId = if (existingProfessor != null) {
                    existingProfessor.id
                } else {
                    // Если не найден, создаём нового преподавателя.
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

                // Создаем запись в previous_theses, используя thesisTitle и professorId.
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