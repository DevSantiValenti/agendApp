package com.analistas.agendApp.service;

import com.analistas.agendApp.dto.TurnoEmailData;
import com.analistas.agendApp.model.Turno;

public interface ITurnoEmailService {
	void notificarAsignacion(Turno turno, String asignadoPor);
	void notificarAnulacion(TurnoEmailData turno);
}
