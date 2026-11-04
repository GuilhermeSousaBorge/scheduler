package com.guilherme.scheduler;

import com.guilherme.scheduler.shared.exception.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.ThrowingController.class)
@Import(GlobalExceptionHandlerTest.ThrowingController.class)
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class ThrowingController {

        @GetMapping("/test/business")
        void business() {
            throw new InvalidDataException("Nome não pode ser vazio");
        }

        @GetMapping("/test/integrity")
        void integrity() {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new RuntimeException("boom");
        }
    }

    @Test
    void businessExceptionUsaStatusEMensagemDaPropriaExcecao() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Nome não pode ser vazio"))
                .andExpect(jsonPath("$.instance").value("/test/business"));
    }

    @Test
    void integrityExceptionUsaStatusEMensagemPadrao() throws Exception {
        mockMvc.perform(get("/test/integrity"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Erro ao processar os dados"))
                .andExpect(jsonPath("$.detail", not(containsString("duplicate"))))
                .andExpect(jsonPath("$.instance").value("/test/integrity"));
    }

    @Test
    void unexpectedExceptionUsaStatusEMensagemPadrao() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Erro interno do servidor"))
                .andExpect(jsonPath("$.detail", not(containsString("boom"))))
                .andExpect(jsonPath("$.instance").value("/test/unexpected"));
    }
}
