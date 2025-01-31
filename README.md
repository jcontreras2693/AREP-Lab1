# TALLER DISEÑO Y ESTRUCTURACIÓN DE APLICACIONES DISTRIBUIDAS EN INTERNET

Durante este taller se escribió un servidor web que soporta múlltiples solicitudes seguidas no concurrentes. El servidor lee los archivos del disco local y retorna todos los archivos solicitados, incluyendo HTML, JavaScript, CSS e imágenes. Se contruyó una aplicación web con las archivos anteriormente mencionados para probar el servidor. En la aplicación se incluye la comunicación asíncrona con unos servicios REST en el backend. NO se usaron frameworks web como Spark o Spring, solo Java y las librerías para manejo de la red.

## Prerequisites

Para ejecutar este proyecto se necesita lo siguiente:

- Java 17 o superior
- Maven 3.8.1 o superior
- Un navegador web

Si cuenta con un IDE de Java en su equipo como NetBeans, IntelliJ, VS Code, entre otros basta con descargar el proyecto y ejecutarlo desde el IDE.

## Instalación

Puede descargar el .zip del proyecto directamente desde GitHub. Si prefiere clonar el repositorio desde la terminal necesitará tener git en su dispositivo, lo puede hacer con el siguiente comando:
```
git clone https://github.com/jcontreras2693/AREP-Lab1.git
``` 
En caso de no contar con un IDE de Java necesitará tener Maven instalado en su dispositivo e ingresar a la carpeta resultante de la descarga y ejecute los siguientes comandos de Maven desde la terminal:
```
mvn clean install
```
```
mvn exec:java -Dexec.mainClass="org.example.PokemonServer"
```
Finalmente ingresa desde un navegador a la dirección [localhost:35000]() para interactuar con la aplicación web.

Ejemplo de la página incial.
![](src/images/base-page.png)

Ejemplo del funcionamiento de la aplicación.
![](src/images/employed-page.png)

## Tests

Las pruebas realizadas verifican los getters y setters de la clase Pokemon. Además del GET y POST del servidor web.

Para ejecutar las pruebas desde la consola se utiliza el siguiente comando:

```
mvn test
```

## Authors

* **Billie Thompson** - *README template* - [PurpleBooth](https://github.com/PurpleBooth)
* **Juan David Contreras** - *Taller 1  AREP* - [AREP-Lab1](https://github.com/jcontreras2693/AREP-Lab1.git)
