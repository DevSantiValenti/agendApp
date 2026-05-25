document.addEventListener("DOMContentLoaded", setupMobileLayout);
document.addEventListener("DOMContentLoaded", setupResponsiveTables);
document.addEventListener("DOMContentLoaded", setupWhatsappTurnoLinks);

function setupMobileLayout() {
	const app = document.querySelector(".app");
	const side = document.querySelector(".side");
	if (!app || !side || document.querySelector(".mobile-topbar")) return;

	const activeLink = side.querySelector("a.active");
	const pageTitle = mobileLabelForLink(activeLink) || activeLink?.title || document.querySelector(".breadcrumbs strong")?.textContent || "Inicio";
	const header = document.createElement("header");
	header.className = "mobile-topbar";
	header.innerHTML = `
		<button class="mobile-menu-button" type="button" data-mobile-menu aria-label="Abrir menú">☰</button>
		<span>${escapeHtml(pageTitle)}</span>
	`;
	const backdrop = document.createElement("div");
	backdrop.className = "mobile-backdrop";
	backdrop.setAttribute("data-mobile-close", "");
	app.before(header);
	app.before(backdrop);

	side.querySelectorAll("a").forEach((link) => {
		if (link.dataset.mobileReady === "true") return;
		const label = link.title || mobileLabelForLink(link) || link.textContent.trim();
		const icon = link.textContent.trim() || "•";
		link.dataset.mobileReady = "true";
		link.innerHTML = `
			<span class="mobile-nav-icon">${escapeHtml(icon)}</span>
			<span class="mobile-nav-label">${escapeHtml(label)}</span>
			<span class="mobile-nav-chevron">›</span>
		`;
	});
}

function mobileLabelForLink(link) {
	const href = link.getAttribute("href") || "";
	if (href.includes("/turnos")) return "Agenda de turnos";
	if (href.includes("/pacientes")) return "Pacientes";
	if (href.includes("/configuracion")) return "Configuración";
	if (href.includes("/logout")) return "Cerrar sesión";
	return "";
}

function setupResponsiveTables() {
	document.querySelectorAll(".content > .table-wrap table, .content > div[style*='display:flex'] .table-wrap table").forEach((table) => {
		const headers = Array.from(table.querySelectorAll("thead th")).map((th) => th.textContent.trim());
		if (!headers.length) return;
		table.querySelectorAll("tbody tr").forEach((row) => {
			Array.from(row.children).forEach((cell, index) => {
				if (!cell.hasAttribute("data-label")) {
					cell.setAttribute("data-label", headers[index] || "");
				}
			});
		});
	});
}


document.addEventListener("click", (event) => {
	const mobileMenuButton = event.target.closest("[data-mobile-menu]");
	if (mobileMenuButton) {
		document.body.classList.add("mobile-nav-open");
		return;
	}

	const mobileClose = event.target.closest("[data-mobile-close]");
	if (mobileClose) {
		document.body.classList.remove("mobile-nav-open");
		return;
	}

	if (event.target.closest(".side a")) {
		document.body.classList.remove("mobile-nav-open");
	}

	const agendaDay = event.target.closest("[data-agenda-date]");
	if (agendaDay && document.getElementById("agendaBody")) {
		event.preventDefault();
		loadAgenda(agendaDay.dataset.agendaDate);
		return;
	}

	const menuButton = event.target.closest("[data-menu-toggle]");
	document.querySelectorAll(".row-menu.open").forEach((menu) => {
		if (!menu.contains(event.target)) {
			menu.classList.remove("open");
			menu.querySelectorAll(".menu-branch.open").forEach((branch) => branch.classList.remove("open"));
		}
	});
	if (menuButton) {
		const rowMenu = menuButton.closest(".row-menu");
		rowMenu.classList.toggle("open");
		if (!rowMenu.classList.contains("open")) {
			rowMenu.querySelectorAll(".menu-branch.open").forEach((branch) => branch.classList.remove("open"));
		}
		return;
	}

	const branchButton = event.target.closest(".menu-branch > .menu-item");
	if (branchButton && window.matchMedia("(max-width: 860px)").matches) {
		event.preventDefault();
		branchButton.closest(".menu-branch").classList.toggle("open");
		return;
	}

	const anularButton = event.target.closest("[data-open-anular]");
	if (anularButton) {
		event.preventDefault();
		openAnularTurnoModal(anularButton.closest("form"));
		return;
	}

	const turnoButton = event.target.closest("[data-open-turno]");
	if (turnoButton) {
		document.querySelectorAll(".row-menu.open").forEach((menu) => menu.classList.remove("open"));
		openTurnoModal(
			turnoButton.dataset.type,
			turnoButton.dataset.dia,
			turnoButton.dataset.hora,
			turnoButton.dataset.profesionalId,
			turnoButton.dataset.sobreturno === "true"
		);
	}

	const presentismoButton = event.target.closest("[data-open-presentismo]");
	if (presentismoButton) {
		openPresentismoModal(presentismoButton.dataset.turnoId, presentismoButton.dataset.hora);
	}

	const detalleButton = event.target.closest("[data-open-detalle-turno]");
	if (detalleButton) {
		openDetalleTurnoModal(detalleButton.closest("tr"));
	}

	const editTurnoButton = event.target.closest("[data-open-edit-turno]");
	if (editTurnoButton) {
		openModificarTurnoModal(editTurnoButton);
	}

	const profesionalButton = event.target.closest("[data-open-profesional]");
	if (profesionalButton) {
		const modal = document.getElementById("modalProfesionalInfo");
		if (modal) {
			modal.classList.add("open");
		}
	}

	const deleteUserButton = event.target.closest("[data-open-delete-user]");
	if (deleteUserButton) {
		openEliminarUsuarioModal(deleteUserButton);
	}

	const deleteProfessionalButton = event.target.closest("[data-open-delete-professional]");
	if (deleteProfessionalButton) {
		openEliminarProfesionalModal(deleteProfessionalButton);
	}

	const applyWeekTramoButton = event.target.closest("[data-apply-week-tramo]");
	if (applyWeekTramoButton) {
		aplicarTramoSemana(applyWeekTramoButton);
	}
});

document.addEventListener("submit", (event) => {
	const form = event.target.closest("[data-turno-save-form]");
	if (!form) return;
	if (form.dataset.submitting === "true") {
		event.preventDefault();
		return;
	}
	form.dataset.submitting = "true";
	form.querySelectorAll("button[type='submit']").forEach((button) => {
		button.disabled = true;
		button.dataset.originalText = button.textContent;
		button.textContent = "Guardando...";
	});
	showTurnoSavingOverlay();
});

function openTurnoModal(type, dia, hora, profesionalId, sobreturno) {
	pendingTurnoContext = { dia, hora, profesionalId, sobreturno };
	if (type !== "nuevo") {
		openPacienteExistenteModal();
		return;
	}
	const modal = document.getElementById(type === "nuevo" ? "modalPacienteNuevo" : "modalPacienteExistente");
	if (!modal) return;
	modal.querySelector("[name='dia']").value = dia;
	modal.querySelector("[name='hora']").value = hora;
	modal.querySelector("[name='sobreturno']").value = sobreturno ? "true" : "false";
	const profesionalInput = modal.querySelector("[name='profesionalId']");
	if (profesionalInput && profesionalId) {
		profesionalInput.value = profesionalId;
	}
	modal.classList.add("open");
}

function openPacienteExistenteModal() {
	const modal = document.getElementById("modalPacienteExistente");
	if (!modal) return;
	const search = document.getElementById("turnoPatientSearch");
	const results = document.getElementById("turnoPatientResults");
	if (search) {
		search.value = "";
		setTimeout(() => search.focus(), 0);
	}
	if (results) {
		results.innerHTML = `<tr><td colspan="8" class="muted">Escribí al menos 2 letras para buscar pacientes.</td></tr>`;
	}
	modal.classList.add("open");
}

function openPresentismoModal(turnoId, hora) {
	const modal = document.getElementById("modalPresentismo");
	if (!modal) return;
	modal.querySelectorAll("[name='id']").forEach((input) => input.value = turnoId);
	const title = modal.querySelector("[data-presentismo-hora]");
	if (title) {
		title.textContent = "Hora - " + hora;
	}
	modal.classList.add("open");
}

function openDetalleTurnoModal(row) {
	const modal = document.getElementById("modalDetalleTurno");
	if (!modal || !row) return;
	document.querySelectorAll(".row-menu.open").forEach((menu) => menu.classList.remove("open"));
	const detail = row.dataset;
	const telefonos = [detail.detailTelefonoCelular, detail.detailTelefonoParticular, detail.detailTelefonoOficina]
		.filter((value) => value && value.trim())
		.join(" / ");
	setDetailField(modal, "profesional", detail.detailProfesional);
	setDetailField(modal, "fecha", detail.detailFecha);
	setDetailField(modal, "hora", detail.detailHora);
	setDetailField(modal, "paciente", detail.detailPaciente);
	setDetailField(modal, "hc", detail.detailHc);
	setDetailField(modal, "presentismo", detail.detailPresentismo);
	setDetailField(modal, "obraSocial", detail.detailObraSocial);
	setDetailField(modal, "plan", detail.detailPlan);
	setDetailField(modal, "afiliado", detail.detailAfiliado);
	setDetailField(modal, "telefonos", telefonos);
	setDetailField(modal, "observacion", detail.detailObservacion);
	setDetailField(modal, "modificadoPor", detail.detailModificadoPor);
	setDetailField(modal, "modificadoEl", detail.detailModificadoEl);
	const editButton = modal.querySelector("[data-open-edit-turno]");
	if (editButton) {
		editButton.dataset.turnoId = detail.detailTurnoId || "";
		editButton.dataset.fecha = detail.detailFechaIso || "";
		editButton.dataset.hora = detail.detailHora || "";
		editButton.dataset.profesionalId = detail.detailProfesionalId || "";
		editButton.dataset.observacion = detail.detailObservacion || "";
		editButton.disabled = !detail.detailTurnoId;
	}
	modal.classList.add("open");
}

function openModificarTurnoModal(button) {
	const modal = document.getElementById("modalModificarTurno");
	const form = modal?.querySelector("[data-edit-turno-form]");
	if (!modal || !form || !button.dataset.turnoId) return;
	document.getElementById("modalDetalleTurno")?.classList.remove("open");
	form.action = `/turnos/${button.dataset.turnoId}/modificar`;
	form.querySelector("[name='dia']").value = button.dataset.fecha || "";
	form.querySelector("[name='hora']").value = button.dataset.hora || "";
	form.querySelector("[name='profesionalId']").value = button.dataset.profesionalId || "";
	form.querySelector("[name='observacion']").value = button.dataset.observacion || "";
	modal.classList.add("open");
}

function setDetailField(modal, name, value) {
	const field = modal.querySelector(`[data-detail-field='${name}']`);
	if (field) {
		field.textContent = value && String(value).trim() ? value : "-";
	}
}

function closeModal(button) {
	button.closest(".modal-backdrop").classList.remove("open");
}

function showTurnoSavingOverlay() {
	const overlay = document.getElementById("turnoSavingOverlay");
	if (!overlay) return;
	overlay.classList.add("open");
	overlay.setAttribute("aria-hidden", "false");
}

function openEliminarUsuarioModal(button) {
	const modal = document.getElementById("modalEliminarUsuario");
	const form = modal?.querySelector("[data-delete-user-form]");
	if (!modal || !form) return;
	form.action = button.dataset.action || "";
	const name = modal.querySelector("[data-delete-user-name]");
	if (name) {
		name.textContent = button.dataset.name || "este usuario";
	}
	modal.classList.add("open");
}

function openEliminarProfesionalModal(button) {
	const modal = document.getElementById("modalEliminarProfesional");
	const form = modal?.querySelector("[data-delete-professional-form]");
	if (!modal || !form) return;
	form.action = button.dataset.action || "";
	const name = modal.querySelector("[data-delete-professional-name]");
	if (name) {
		name.textContent = button.dataset.name || "este profesional";
	}
	modal.classList.add("open");
}

function openAnularTurnoModal(form) {
	if (!form) return;
	pendingAnularForm = form;
	document.querySelectorAll(".row-menu.open").forEach((menu) => menu.classList.remove("open"));
	document.getElementById("modalAnularTurno")?.classList.add("open");
}

function aplicarTramoSemana(button) {
	const tramo = button.dataset.applyWeekTramo;
	const rows = Array.from(document.querySelectorAll("[data-professional-schedule-row]"));
	const source = rows.find((row) => row.dataset.scheduleDay === "Lunes" && row.dataset.scheduleTramo === tramo);
	if (!source) return;

	const desde = source.querySelector("input[name$='.horaDesde']")?.value || "";
	const hasta = source.querySelector("input[name$='.horaHasta']")?.value || "";
	const online = Boolean(source.querySelector("input[name$='.agendaOnline']")?.checked);
	if (!desde || !hasta) {
		alert(`Completá el tramo ${tramo} del lunes antes de aplicarlo.`);
		return;
	}

	const weekdays = new Set(["Lunes", "Martes", "Miercoles", "Jueves", "Viernes"]);
	rows
		.filter((row) => weekdays.has(row.dataset.scheduleDay) && row.dataset.scheduleTramo === tramo)
		.forEach((row) => {
			const desdeInput = row.querySelector("input[name$='.horaDesde']");
			const hastaInput = row.querySelector("input[name$='.horaHasta']");
			const onlineInput = row.querySelector("input[name$='.agendaOnline']");
			if (desdeInput) desdeInput.value = desde;
			if (hastaInput) hastaInput.value = hasta;
			if (onlineInput) onlineInput.checked = online;
		});

	const originalText = button.textContent;
	button.textContent = "Aplicado";
	button.disabled = true;
	window.setTimeout(() => {
		button.textContent = originalText;
		button.disabled = false;
	}, 1200);
}

document.addEventListener("keydown", (event) => {
	if (event.key === "Escape") {
		document.querySelectorAll(".modal-backdrop.open").forEach((modal) => modal.classList.remove("open"));
	}
});

const agendaDateInput = document.getElementById("agendaDateInput");
const agendaBody = document.getElementById("agendaBody");
let pendingTurnoContext = null;
let pendingAnularForm = null;

document.getElementById("confirmAnularTurno")?.addEventListener("click", () => {
	if (pendingAnularForm) {
		pendingAnularForm.submit();
	}
});

if (agendaDateInput && agendaBody) {
	agendaDateInput.addEventListener("change", () => {
		if (agendaDateInput.value) {
			loadAgenda(agendaDateInput.value);
		}
	});
}

async function loadAgenda(fecha) {
	const body = document.getElementById("agendaBody");
	if (!body || !fecha) return;

	body.classList.add("loading");
	const profesionalId = getTopbarValue("profesionalId");
	const especialidadId = getTopbarValue("especialidadId");
	const params = new URLSearchParams({ fecha });
	if (profesionalId) params.set("profesionalId", profesionalId);

	try {
		const response = await fetch(`/turnos/agenda?${params.toString()}`, {
			headers: { "Accept": "application/json" }
		});
		if (!response.ok) throw new Error("No se pudo cargar la agenda");
		const agenda = await response.json();
		renderAgenda(agenda);
		updateAgendaUrl(agenda.fecha, profesionalId, especialidadId);
	} catch (error) {
		console.error(error);
	} finally {
		body.classList.remove("loading");
	}
}

function renderAgenda(agenda) {
	const dateInput = document.getElementById("agendaDateInput");
	const dateText = document.getElementById("agendaDateText");
	if (dateInput) dateInput.value = agenda.fecha;
	if (dateText) dateText.textContent = agenda.fechaTexto;
	renderAgendaDays(agenda);
	renderAgendaRows(agenda);
	setupWhatsappTurnoLinks();
	updateAgendaHiddenInputs(agenda.fecha);
}

function renderAgendaDays(agenda) {
	const tabs = document.getElementById("agendaDayTabs");
	if (!tabs) return;

	const profesionalId = getTopbarValue("profesionalId");
	const especialidadId = getTopbarValue("especialidadId");
	const today = tabs.querySelector("a")?.dataset.agendaDate || toIsoLocalDate(new Date());
	const links = [
		{ etiqueta: "Hoy", fecha: today, active: agenda.fecha === today, selected: false },
		...(agenda.diasSemana || []).map((dia) => ({
			etiqueta: dia.etiqueta,
			fecha: dia.fecha,
			active: false,
			selected: Boolean(dia.seleccionado)
		}))
	];

	tabs.innerHTML = links.map((link) => {
		const params = new URLSearchParams({ fecha: link.fecha });
		if (profesionalId) params.set("profesionalId", profesionalId);
		if (especialidadId) params.set("especialidadId", especialidadId);
		const className = [link.active ? "active" : "", link.selected ? "selected" : ""].filter(Boolean).join(" ");
		return `<a href="/turnos?${params.toString()}" data-agenda-date="${link.fecha}" class="${className}">${escapeHtml(link.etiqueta)}</a>`;
	}).join("");
}

function renderAgendaRows(agenda) {
	const body = document.getElementById("agendaBody");
	if (!body) return;
	const profesionalId = getTopbarValue("profesionalId");
	const slots = agenda.slots || [];
	if (!slots.length) {
		body.innerHTML = `
			<tr class="agenda-empty-row">
				<td colspan="7">
					<div class="agenda-empty-state">
						<div class="agenda-empty-icon">⌕</div>
						<div>
							<strong>Ups!</strong>
							<span>No hay datos para mostrar, elegí otro día y/o profesional.</span>
						</div>
					</div>
				</td>
			</tr>
		`;
		return;
	}
	body.innerHTML = slots.map((slot) => renderAgendaRow(slot, agenda.fecha, profesionalId)).join("");
}

function renderAgendaRow(slot, fecha, profesionalId) {
	const turno = slot.turno;
	const bloqueado = isBlockedSlot(turno);
	const detail = buildTurnoDetailDataset(turno, slot.hora, fecha);
	return `
		<tr class="${bloqueado ? "slot-blocked" : ""}" ${detail}>
			<td class="row-menu">
				<button class="dots" type="button" data-menu-toggle title="Opciones">&vellip;</button>
				<div class="context-menu">
					<div class="menu-title">Turnos</div>
					<div class="menu-branch">
						<button class="menu-item" type="button">
							<span class="menu-label"><span class="menu-icon">◷</span> Nuevo turno</span><span>›</span>
						</button>
						<div class="context-submenu">
							<button class="menu-item" type="button"
								data-open-turno data-type="existente" data-sobreturno="false"
								data-dia="${fecha}" data-hora="${slot.hora}" data-profesional-id="${profesionalId || ""}">
								<span class="menu-label"><span class="menu-icon">☷</span> Paciente existente</span>
							</button>
							<button class="menu-item" type="button"
								data-open-turno data-type="nuevo" data-sobreturno="false"
								data-dia="${fecha}" data-hora="${slot.hora}" data-profesional-id="${profesionalId || ""}">
								<span class="menu-label"><span class="menu-icon">＋</span> Paciente nuevo</span>
							</button>
						</div>
					</div>
					<button class="menu-item" type="button"
						data-open-turno data-type="existente" data-sobreturno="true"
						data-dia="${fecha}" data-hora="${slot.hora}" data-profesional-id="${profesionalId || ""}">
						<span class="menu-label"><span class="menu-icon">▣</span> Nuevo sobreturno</span>
					</button>
					${turno ? `
						<button class="menu-item" type="button" data-open-detalle-turno>
							<span class="menu-label"><span class="menu-icon">ⓘ</span> Ver detalles</span>
						</button>
					` : ""}
					${turno ? `
						<form action="/turnos/${turno.id}/anular" method="post">
							<input type="hidden" name="dia" value="${fecha}">
							<input type="hidden" name="profesionalId" value="${profesionalId || ""}">
							<button class="menu-item danger" type="button" data-open-anular>
								<span class="menu-label"><span class="menu-icon">⊘</span> Anular turno</span>
							</button>
						</form>
					` : `
						<form action="/turnos/anular-horario" method="post">
							<input type="hidden" name="dia" value="${fecha}">
							<input type="hidden" name="hora" value="${escapeHtml(slot.hora)}">
							<input type="hidden" name="profesionalId" value="${profesionalId || ""}">
							<button class="menu-item danger" type="button" data-open-anular>
								<span class="menu-label"><span class="menu-icon">⊘</span> Anular turno</span>
							</button>
						</form>
					`}
				</div>
			</td>
			<td><span>${escapeHtml(slot.hora)}</span>${bloqueado ? `<span class="slot-block-dot" aria-hidden="true"></span>` : ""}</td>
			<td>${bloqueado ? "" : renderPresentismoButton(turno, slot.hora)}</td>
			<td>${bloqueado ? renderBlockedSlotLabel() : renderPaciente(turno?.paciente, turno?.observacion, turno?.profesional, fecha, slot.hora)}</td>
			<td>${renderObraSocial(turno)}</td>
			<td>${bloqueado ? "" : escapeHtml(turno?.observacion || "")}</td>
			<td>${renderModificacion(turno)}</td>
		</tr>
	`;
}

function isBlockedSlot(turno) {
	return turno?.presentismo === "CANCELADO" && !turno.paciente && turno.observacion === "NO CITAR ESTE TURNO";
}

function renderBlockedSlotLabel() {
	return `
		<div class="slot-block-label">
			<span class="slot-info-icon">i</span>
			<strong>NO CITAR ESTE TURNO</strong>
		</div>
	`;
}

function renderPresentismoButton(turno, hora) {
	if (!turno) return "";
	return `<button type="button"
		class="status-pill status-${escapeHtml(turno.presentismoCssClass || "sin-confirmar")}"
		data-status-code="${escapeHtml(turno.presentismoCodigo || statusCode(turno.presentismoEtiqueta))}"
		data-open-presentismo
		data-turno-id="${turno.id}"
		data-hora="${escapeHtml(hora)}">${escapeHtml(turno.presentismoEtiqueta || "Sin confirmar")}</button>`;
}

function statusCode(label) {
	const normalized = String(label || "").toUpperCase();
	if (normalized.includes("CON AVISO")) return "ACA";
	if (normalized.includes("SIN AVISO")) return "ASA";
	return normalized.substring(0, 3);
}

function buildTurnoDetailDataset(turno, hora, fecha) {
	const paciente = turno?.paciente || {};
	const attrs = {
		"data-detail-turno-id": turno?.id,
		"data-detail-fecha-iso": fecha,
		"data-detail-fecha": formatDateSlash(fecha),
		"data-detail-hora": hora,
		"data-detail-profesional-id": turno?.profesionalId,
		"data-detail-profesional": turno?.profesional,
		"data-detail-paciente-id": paciente.id,
		"data-detail-paciente": paciente.nombreCompleto,
		"data-detail-hc": paciente.historiaClinica,
		"data-detail-presentismo": turno?.presentismoEtiqueta,
		"data-detail-obra-social": turno?.obraSocial,
		"data-detail-plan": turno?.plan,
		"data-detail-afiliado": turno?.numeroAfiliado,
		"data-detail-telefono-celular": paciente.telefonoCelular,
		"data-detail-telefono-particular": paciente.telefonoParticular,
		"data-detail-telefono-oficina": paciente.telefonoOficina,
		"data-detail-observacion": turno?.observacion,
		"data-detail-modificado-por": turno?.dadoModificadoPor,
		"data-detail-modificado-el": turno?.fechaModificacion
	};
	return Object.entries(attrs)
		.map(([name, value]) => `${name}="${escapeHtml(value || "")}"`)
		.join(" ");
}

function formatDateSlash(value) {
	if (!value || !value.includes("-")) return value || "";
	const [year, month, day] = value.split("-");
	return `${day}/${month}/${year}`;
}

function renderPaciente(paciente, observacion, professional, fecha, hora) {
	if (!paciente) return "";
	return `
		<div>
			${paciente.historiaClinica ? `<span class="hc-pill">H.C: ${escapeHtml(paciente.historiaClinica)}</span>` : ""}
			<div class="patient-name-line">
				<span>${escapeHtml(paciente.nombreCompleto || "")}</span>
				${renderWhatsappTurnoLink(paciente, professional, fecha, hora)}
			</div>
			${observacion ? `<div class="agenda-mobile-observation">${escapeHtml(observacion)}</div>` : ""}
			${paciente.telefonoCelular ? `<div class="muted">Tel: ${escapeHtml(paciente.telefonoCelular)}</div>` : ""}
		</div>
	`;
}

function renderWhatsappTurnoLink(paciente, professional, fecha, hora) {
	if (!paciente?.telefonoCelular) return "";
	return `
		<a class="whatsapp-turno-link"
			href="#"
			target="_blank"
			rel="noopener"
			title="Enviar recordatorio por WhatsApp"
			aria-label="Enviar recordatorio por WhatsApp"
			data-whatsapp-turno
			data-whatsapp-phone="${escapeHtml(paciente.telefonoCelular)}"
			data-whatsapp-patient="${escapeHtml(paciente.nombreCompleto || "")}"
			data-whatsapp-professional="${escapeHtml(professional || "")}"
			data-whatsapp-date="${escapeHtml(fecha || "")}"
			data-whatsapp-hour="${escapeHtml(hora || "")}">
			<img src="/img/whatsapp.png" alt="">
		</a>
	`;
}

function setupWhatsappTurnoLinks() {
	document.querySelectorAll("[data-whatsapp-turno]").forEach((link) => {
		const phone = normalizeWhatsappPhone(link.dataset.whatsappPhone);
		if (!phone) {
			link.setAttribute("hidden", "");
			return;
		}
		const message = whatsappTurnoMessage(link.dataset);
		link.href = `https://wa.me/${phone}?text=${encodeURIComponent(message)}`;
		link.removeAttribute("hidden");
	});
}

function normalizeWhatsappPhone(value) {
	let digits = String(value || "").replace(/\D/g, "");
	if (!digits) return "";
	if (digits.startsWith("00")) digits = digits.slice(2);
	while (digits.startsWith("0")) digits = digits.slice(1);
	if (digits.startsWith("549") || digits.startsWith("54")) return digits;
	if (digits.length === 10) return `549${digits}`;
	return digits;
}

function whatsappTurnoMessage(data) {
	const patient = data.whatsappPatient || "";
	const professional = data.whatsappProfessional || "";
	const date = formatWhatsappDate(data.whatsappDate);
	const hour = data.whatsappHour || "";
	return `Hola! ${patient}, te recordamos que tienes un turno con el Dr/a ${professional} el día ${date} a las ${hour} en Av Alvear 856.`;
}

function formatWhatsappDate(value) {
	if (!value) return "";
	const [year, month, day] = String(value).split("-").map(Number);
	if (!year || !month || !day) return value;
	const date = new Date(year, month - 1, day);
	return new Intl.DateTimeFormat("es-AR", {
		weekday: "long",
		day: "numeric",
		month: "long",
		year: "numeric"
	}).format(date);
}

function renderObraSocial(turno) {
	if (!turno) return "";
	return `
		<div>
			<div>${escapeHtml(turno.obraSocial || "")}</div>
			${turno.plan ? `<div class="muted">Plan: ${escapeHtml(turno.plan)}</div>` : ""}
		</div>
	`;
}

function renderModificacion(turno) {
	if (!turno) return "";
	return `
		<div>
			<strong>${escapeHtml(turno.dadoModificadoPor || "")}</strong>
			${turno.fechaModificacion ? `<div class="muted">${escapeHtml(turno.fechaModificacion)}</div>` : ""}
		</div>
	`;
}

function updateAgendaHiddenInputs(fecha) {
	document.querySelectorAll("#modalPresentismo [name='dia']").forEach((input) => input.value = fecha);
	document.querySelectorAll("#modalPresentismo [name='profesionalId']").forEach((input) => input.value = getTopbarValue("profesionalId"));
}

function updateAgendaUrl(fecha, profesionalId, especialidadId) {
	const params = new URLSearchParams({ fecha });
	if (profesionalId) params.set("profesionalId", profesionalId);
	if (especialidadId) params.set("especialidadId", especialidadId);
	window.history.replaceState({}, "", `/turnos?${params.toString()}`);
}

function getTopbarValue(name) {
	return document.querySelector(`.topbar [name='${name}']`)?.value || "";
}

const turnoPatientSearch = document.getElementById("turnoPatientSearch");
const turnoPatientResults = document.getElementById("turnoPatientResults");
const turnoPatientSearchButton = document.getElementById("turnoPatientSearchButton");
const turnoPatientContains = document.getElementById("turnoPatientContains");
let turnoPatientSearchTimer;

if (turnoPatientSearch && turnoPatientResults) {
	turnoPatientSearch.addEventListener("input", () => {
		clearTimeout(turnoPatientSearchTimer);
		const query = turnoPatientSearch.value.trim();
		if (query.length < 2) {
			turnoPatientResults.innerHTML = `<tr><td colspan="8" class="muted">Escribí al menos 2 letras para buscar pacientes.</td></tr>`;
			return;
		}
		turnoPatientSearchTimer = setTimeout(() => searchTurnoPatients(query), 180);
	});
}

if (turnoPatientSearchButton && turnoPatientSearch) {
	turnoPatientSearchButton.addEventListener("click", () => {
		const query = turnoPatientSearch.value.trim();
		if (query.length >= 2) {
			searchTurnoPatients(query);
		} else {
			turnoPatientSearch.focus();
		}
	});
}

if (turnoPatientContains && turnoPatientSearch) {
	turnoPatientContains.addEventListener("change", () => {
		const query = turnoPatientSearch.value.trim();
		if (query.length >= 2) {
			searchTurnoPatients(query);
		}
	});
}

async function searchTurnoPatients(query) {
	if (!turnoPatientResults) return;
	turnoPatientResults.innerHTML = `<tr><td colspan="8" class="muted">Buscando...</td></tr>`;
	const response = await fetch(`/pacientes/buscar?q=${encodeURIComponent(query)}`, {
		headers: { "Accept": "application/json" }
	});
	if (!response.ok) {
		turnoPatientResults.innerHTML = `<tr><td colspan="8" class="muted">No se pudo buscar pacientes.</td></tr>`;
		return;
	}
	let patients = await response.json();
	if (!turnoPatientContains?.checked) {
		const normalizedQuery = normalizeText(query);
		patients = patients.filter((patient) => normalizeText(patient.nombreCompleto).startsWith(normalizedQuery));
	}
	renderTurnoPatientResults(patients);
}

function renderTurnoPatientResults(patients) {
	if (!turnoPatientResults) return;
	if (!patients.length) {
		turnoPatientResults.innerHTML = `<tr><td colspan="8" class="muted">Sin coincidencias.</td></tr>`;
		return;
	}
	turnoPatientResults.innerHTML = patients.map((patient, index) => `
		<tr class="patient-picker-row" tabindex="0" data-patient-index="${index}">
			<td data-label="Paciente"><strong>${escapeHtml(patient.nombreCompleto || "Sin nombre")}</strong></td>
			<td data-label="H.C.">${patient.historiaClinica ? `<span class="hc-pill">${escapeHtml(patient.historiaClinica)}</span>` : "-"}</td>
			<td data-label="Obra social">${escapeHtml(patient.obraSocial || "PRIVADOS")}</td>
			<td data-label="Plan">${escapeHtml(patient.plan || "PLAN UNICO")}</td>
			<td data-label="Nacimiento">${escapeHtml(formatPatientDate(patient.fechaNacimiento) || "-")}</td>
			<td data-label="Afiliado">${escapeHtml(patient.numeroAfiliado || "-")}</td>
			<td data-label="Documento">${escapeHtml(patient.documento || "-")}</td>
			<td data-label="Domicilio">${escapeHtml(patient.domicilio || "-")}</td>
		</tr>
	`).join("");
	turnoPatientResults.querySelectorAll("[data-patient-index]").forEach((row) => {
		const select = () => selectTurnoPatient(patients[Number(row.dataset.patientIndex)]);
		row.addEventListener("click", select);
		row.addEventListener("keydown", (event) => {
			if (event.key === "Enter" || event.key === " ") {
				event.preventDefault();
				select();
			}
		});
	});
}

function selectTurnoPatient(patient) {
	if (!patient || !pendingTurnoContext) return;
	document.getElementById("modalPacienteExistente")?.classList.remove("open");
	const modal = document.getElementById("modalTurnoObservacion");
	if (!modal) return;
	const form = modal.querySelector("form");
	if (form) {
		form.reset();
		form.querySelector("[name='dia']").value = pendingTurnoContext.dia;
		form.querySelector("[name='hora']").value = pendingTurnoContext.hora;
		form.querySelector("[name='sobreturno']").value = pendingTurnoContext.sobreturno ? "true" : "false";
		form.querySelector("[name='pacienteId']").value = patient.id;
		const profesionalInput = form.querySelector("[name='profesionalId']");
		if (profesionalInput && pendingTurnoContext.profesionalId) {
			profesionalInput.value = pendingTurnoContext.profesionalId;
		}
	}
	const name = modal.querySelector("[data-selected-patient-name]");
	const meta = modal.querySelector("[data-selected-patient-meta]");
	if (name) {
		name.textContent = patient.nombreCompleto || "Sin nombre";
	}
	if (meta) {
		meta.textContent = [
			patient.historiaClinica ? `H.C: ${patient.historiaClinica}` : null,
			patient.documento ? `DNI: ${patient.documento}` : null,
			[patient.obraSocial, patient.plan].filter(Boolean).join(" / ")
		].filter(Boolean).join(" · ");
	}
	modal.classList.add("open");
}

function normalizeText(value) {
	return String(value || "")
		.normalize("NFD")
		.replace(/[\u0300-\u036f]/g, "")
		.toLowerCase()
		.trim();
}

function toIsoLocalDate(date) {
	const year = date.getFullYear();
	const month = String(date.getMonth() + 1).padStart(2, "0");
	const day = String(date.getDate()).padStart(2, "0");
	return `${year}-${month}-${day}`;
}

const patientSearch = document.getElementById("patientSearch");
const patientResults = document.getElementById("patientSearchResults");
let patientSearchTimer;

if (patientSearch && patientResults) {
	patientSearch.addEventListener("input", () => {
		clearTimeout(patientSearchTimer);
		const query = patientSearch.value.trim();
		if (query.length < 2) {
			patientResults.innerHTML = "";
			patientResults.classList.remove("open");
			return;
		}
		patientSearchTimer = setTimeout(() => searchPatients(query), 180);
	});
}

async function searchPatients(query) {
	const response = await fetch(`/pacientes/buscar?q=${encodeURIComponent(query)}`, {
		headers: { "Accept": "application/json" }
	});
	if (!response.ok) return;
	const patients = await response.json();
	renderPatientResults(patients);
}

function renderPatientResults(patients) {
	if (!patientResults) return;
	if (!patients.length) {
		patientResults.innerHTML = `<div class="patient-result-empty">Sin coincidencias</div>`;
		patientResults.classList.add("open");
		return;
	}
	patientResults.innerHTML = patients.map((patient) => `
		<button type="button" class="patient-result" data-patient-id="${patient.id}">
			<strong>${escapeHtml(patient.nombreCompleto || "Sin nombre")}</strong>
			<span>H.C: ${escapeHtml(patient.historiaClinica || "-")} · ${escapeHtml(patient.documento || "-")}</span>
			<small>${escapeHtml([patient.obraSocial, patient.plan].filter(Boolean).join(" / ") || "Sin obra social")}</small>
		</button>
	`).join("");
	patientResults.classList.add("open");
	patientResults.querySelectorAll("[data-patient-id]").forEach((button) => {
		button.addEventListener("click", () => loadPatientDetail(button.dataset.patientId));
	});
}

async function loadPatientDetail(patientId) {
	const response = await fetch(`/pacientes/${patientId}/detalle`, {
		headers: { "Accept": "application/json" }
	});
	if (!response.ok) return;
	const patient = await response.json();
	fillPatientDetail(patient);
	if (patientResults) {
		patientResults.classList.remove("open");
	}
	if (patientSearch) {
		patientSearch.value = patient.nombreCompleto || "";
	}
}

function fillPatientDetail(patient) {
	document.getElementById("patientEmptyState")?.setAttribute("hidden", "");
	document.getElementById("patientDetail")?.removeAttribute("hidden");
	document.getElementById("patientFooter")?.removeAttribute("hidden");
	document.getElementById("patientSummary")?.classList.remove("empty");

	document.querySelectorAll("[data-patient]").forEach((field) => {
		setFieldValue(field, patient[field.dataset.patient]);
	});
	document.querySelectorAll("[data-patient-date]").forEach((field) => {
		setFieldValue(field, formatPatientDate(patient[field.dataset.patientDate]));
	});
	document.querySelectorAll("[data-patient-bool]").forEach((field) => {
		field.checked = Boolean(patient[field.dataset.patientBool]);
	});
	document.querySelectorAll("[data-patient-check]").forEach((field) => {
		field.checked = Boolean(patient[field.dataset.patientCheck]);
	});

	const hc = document.querySelector("[data-patient-hc]");
	if (hc) hc.textContent = patient.historiaClinica ? `H.C: ${patient.historiaClinica}` : "H.C: -";

	const affiliation = document.querySelector("[data-patient-affiliation]");
	if (affiliation) {
		affiliation.textContent = [patient.obraSocial, patient.plan].filter(Boolean).join(" / ") || "Sin obra social";
	}

	const balance = document.querySelector("[data-patient-balance]");
	if (balance) {
		balance.textContent = formatMoney(patient.saldoAcreedor);
	}

	document.querySelectorAll("[data-patient-edit-link]").forEach((editLink) => {
		if (patient.id) {
			editLink.href = `/pacientes/${patient.id}`;
			editLink.removeAttribute("hidden");
			editLink.classList.remove("disabled");
			editLink.removeAttribute("aria-disabled");
		} else {
			editLink.href = "#";
			editLink.setAttribute("hidden", "");
			editLink.classList.add("disabled");
			editLink.setAttribute("aria-disabled", "true");
		}
	});
	const deleteForm = document.getElementById("patientDeleteForm");
	if (deleteForm) {
		deleteForm.action = `/pacientes/${patient.id}/eliminar`;
		deleteForm.onsubmit = () => confirm("¿Eliminar este paciente?");
	}
}

function setFieldValue(field, value) {
	const normalized = value ?? "";
	if ("value" in field) {
		field.value = normalized;
	} else {
		field.textContent = normalized;
	}
}

function formatPatientDate(value) {
	if (!value) return "";
	const [year, month, day] = value.split("-");
	return `${day}/${month}/${year}`;
}

function formatMoney(value) {
	const number = Number(value || 0);
	return number.toLocaleString("es-AR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(value) {
	return String(value)
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;")
		.replaceAll("'", "&#039;");
}
