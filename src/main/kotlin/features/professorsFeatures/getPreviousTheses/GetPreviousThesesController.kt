package com.example.features.professorsFeatures.getPreviousTheses
import com.example.database.previous_theses.PreviousTheses
import com.example.database.professors.Professors
import com.example.features.getPreviousTheses.GetPreviousThesesRequestRemote
import com.example.features.getPreviousTheses.GetPreviousThesesResponseRemote
import com.example.features.getPreviousTheses.PreviousThesisRemote
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*


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
}