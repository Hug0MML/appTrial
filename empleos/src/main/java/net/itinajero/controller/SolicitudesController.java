package net.itinajero.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.itinajero.model.Solicitud;
import net.itinajero.model.Usuario;
import net.itinajero.model.Vacante;
import net.itinajero.service.ISolicitudesService;
import net.itinajero.service.IUsuariosService;
import net.itinajero.service.IVacantesService;
import net.itinajero.util.Utileria;


@Controller
@RequestMapping("/solicitudes")
public class SolicitudesController {
	
	@Value("${empleosapp.ruta.cv}")
	private String rutaCv;
	
	@Autowired
	private IUsuariosService serviceUsuario;
	
	@Autowired
	private IVacantesService serviceVacante;
	
	@Autowired
	private ISolicitudesService serviceSolicitudes;
	
	@GetMapping("/indexPaginate")
	public String mostrarIndexPaginado(Model model, Pageable pageable)  {
		Page<Solicitud> lista = serviceSolicitudes.buscarTodas(pageable);
		model.addAttribute("solicitudes", lista);
		return "solicitudes/listSolicitudes";
	}
	
	@GetMapping("/create/{idVacante}")
	public String crear(@PathVariable("idVacante")Integer idVacante, Model model) {
		
		Vacante vacante = serviceVacante.buscarPorId(idVacante);
		System.out.println("idVacante: " + idVacante);
		model.addAttribute("vacante", vacante);
		return "solicitudes/formSolicitud";
		
	}
	
	@PostMapping("/save")
	public String guardar(Solicitud solicitud, BindingResult result,@RequestParam("archivoCV") MultipartFile multipart, Authentication authentication,
			RedirectAttributes attributes) {
		
		String username = authentication.getName();
		
		if(result.hasErrors()) {
			System.out.println("Pinche Puto Error");
			return "solicitudes/formSolicitud";
		}	
			
		if(! multipart.isEmpty()) {
			String nombreArchivo = Utileria.guardarArchivo(multipart, rutaCv);
			if(nombreArchivo !=null) {
				solicitud.setArchivo(nombreArchivo);
			}
		}
		
		Usuario usuario = serviceUsuario.buscarPorUsername(username);
		solicitud.setUsuario(usuario);
		
		serviceSolicitudes.guardar(solicitud);
		attributes.addFlashAttribute("msg", "Gracias por su interes");
		
		System.out.println("Solicitud: " + solicitud);
		return "redirect:/";
	}
	@GetMapping("/delete/{id}")
	public String eliminar(@PathVariable("id") int idSolicitud, RedirectAttributes attributes) {
		
		serviceSolicitudes.eliminar(idSolicitud);
		attributes.addFlashAttribute("msg", "Solicitud eliminada");
		
		return "redirect:/solicitudes/indexPaginate";
		
		
		
	}
}
