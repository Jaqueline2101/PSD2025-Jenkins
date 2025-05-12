package pe.edu.upeu.sysalmacen.control;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.cloudinary.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.sysalmacen.dtos.report.ProdMasVendidosDTO;
import pe.edu.upeu.sysalmacen.modelo.MediaFile;
import pe.edu.upeu.sysalmacen.servicio.IMediaFileService;
import pe.edu.upeu.sysalmacen.servicio.IProductoService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reporte")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class); // Logger

    private final IProductoService productoService;
    private final IMediaFileService mfService;
    private final Cloudinary cloudinary;

    @GetMapping("/pmvendidos")
    public List<ProdMasVendidosDTO> getProductosMasVendidos() {
        return productoService.obtenerProductosMasVendidos();
    }

    @GetMapping(value = "/generateReport", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> generateReport() {
        try {
            byte[] data = productoService.generateReport();
            if (data == null || data.length == 0) {
                return ResponseEntity.status(500).body("No data generated".getBytes());
            }
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            // Registra el error y proporciona detalles
            logger.error("Error generating report: {}", e.getMessage());
            return ResponseEntity.status(500).body(("Error generating report: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping(value = "/readFile/{idFile}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> readFile(@PathVariable("idFile") Long idFile) {
        try {
            MediaFile mediaFile = mfService.findById(idFile);
            if (mediaFile == null || mediaFile.getContent() == null) {
                return ResponseEntity.status(404).body("File not found".getBytes());
            }
            byte[] data = mediaFile.getContent();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            // Registra el error de lectura
            logger.error("Error reading file: {}", e.getMessage());
            return ResponseEntity.status(500).body(("Error reading file: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping(value = "/saveFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveFile(@RequestParam("file") MultipartFile multipartFile) {
        try {
            MediaFile mf = new MediaFile();
            mf.setContent(multipartFile.getBytes());
            mf.setFileName(multipartFile.getOriginalFilename());
            mf.setFileType(multipartFile.getContentType());
            mfService.save(mf);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Registra el error al guardar el archivo
            logger.error("Error saving file: {}", e.getMessage());
            return ResponseEntity.status(500).build(); // Handle file saving error
        }
    }

    @PostMapping(value = "/saveFileCloud", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveFileCloud(@RequestParam("file") MultipartFile multipartFile) {
        try {
            File file = convertToFile(multipartFile);
            Map<String, Object> response = cloudinary.uploader().upload(file, ObjectUtils.asMap("resource_type", "auto"));
            JSONObject json = new JSONObject(response);
            String url = json.getString("url");
            logger.info("File uploaded successfully to Cloudinary: {}", url); // Log the URL
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Registra el error al cargar el archivo en Cloudinary
            logger.error("Error uploading file to Cloudinary: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error uploading file to Cloudinary: " + e.getMessage());
        }
    }

    private File convertToFile(MultipartFile multipartFile) throws Exception {
    try {
        // Crear el archivo a partir del multipartFile
        File archivo = new File(multipartFile.getOriginalFilename());
        
        // Intentar escribir el archivo en el sistema
        try (FileOutputStream flujoDeSalida = new FileOutputStream(archivo)) {
            flujoDeSalida.write(multipartFile.getBytes());
        }

        return archivo;
    } catch (Exception e) {
        // Registra el error y lanza una nueva excepción con más contexto
        logger.error("Error converting MultipartFile to File: {}", multipartFile.getOriginalFilename(), e);
        
        // Lanza una excepción personalizada con más información sobre el error
        throw new FileConversionException("Error converting MultipartFile to File: " + multipartFile.getOriginalFilename(), e);
        }
    }

}
