public class Alumno {
    private int id;
    private String nombre;
    private int edad;

    public Alumno(int id, String nombre, int edad) {
        this.id = id;
        this.nombre = nombre;
        this.edad = edad;
    }

    // Getters para poder leer los datos
    public int getId() { 
        return id; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    
    public int getEdad() { 
        return edad; 
    }
}