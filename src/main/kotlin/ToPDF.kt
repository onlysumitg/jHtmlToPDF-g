import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.tool.xml.XMLWorkerHelper

import org.apache.commons.io.FileUtils.toFile
import java.awt.SystemColor.info
import java.nio.file.Files.createDirectories
import java.nio.file.Files.createFile
import java.nio.file.Files.notExists
import java.nio.file.Files
import java.nio.file.Paths
import org.jsoup.Jsoup
import org.zeroturnaround.zip.ZipUtil
import java.io.*

//java -cp jHtmlToPDF-g.jar ToPDFKt c:/html

fun main(args: Array<String>) {
    println("************** STARTING *********************")

    var inputPath = ""
    if(args.size<1)
    {
        println("************** NO PARAM ********************* ${args.size}")

    }
    else
    {
        println("************** PARAMS ********************* ${args.size}")
        inputPath = args[0].trim()


    }

    inputPath = if(inputPath.isNotEmpty())  inputPath else "."
    if(inputPath.endsWith("/"))
    {
        inputPath = inputPath.substringBeforeLast("/")
    }

    if(inputPath.endsWith("\\"))
    {
        inputPath = inputPath.substringBeforeLast("\\")
    }
    val folder = File(inputPath)


    if(folder.isDirectory)
    {
        println("************** Complete folder *********************")

        val files = folder.listFiles()

        files?.forEach { it->

            generatePDFFromHTML(it,inputPath)
        }
    }
    else
    {
        println("************** just a file *********************")
        inputPath = folder.parent

        generatePDFFromHTML(folder,inputPath)
    }

    zipFolder(inputPath)

    println("************** FINISHED *********************")

}



    private fun generatePDFFromHTML(file: File, outPath:String) {
        println("Processing ${file.name}")
        var fileNameWithoutExt=""

        var formattedHtmlString =""

        if(file.isDirectory)
        {
            return
        }
        if(file.name.toUpperCase().endsWith(".HTML") || file.name.toUpperCase().endsWith(".HTM") )
        {
            fileNameWithoutExt = file.name.substringBeforeLast(".")

              formattedHtmlString = toXHTML(file)

        }
        else
        {
            println("NOT A VALID HTML FILE EXTENSIONS")

        }

        if (fileNameWithoutExt.isEmpty())
        {
            return;
        }
        val document = Document()

        val pdfFileName ="$outPath/pdf/$fileNameWithoutExt.pdf"

            createOrRetrieve("$outPath/pdf")

        val writer = PdfWriter.getInstance(
            document,
            FileOutputStream(pdfFileName)
        )
        document.open()
        XMLWorkerHelper.getInstance().parseXHtml(
            writer, document,
            stringToInputStream(formattedHtmlString)//FileInputStream(file.absoluteFile)
        )
        document.close()

        println("Completed  ${file.name}")
    }



private fun createOrRetrieve(target: String) {

    val path = Paths.get(target)

    try {
        if (Files.notExists(path)) {
            // LOG.info("Target file \"$target\" will be created.")
            println("Creating blank $path")
            Files.createFile(Files.createDirectories(path))
        }
    } catch (e: Exception)
    {

    }
    //LOG.info("Target file \"$target\" will be retrieved.")
    return
}


private fun toXHTML(html: File): String {
    val document:org.jsoup.nodes.Document = Jsoup.parse(html,"UTF-8")

    document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
    return document.html()
}


fun stringToInputStream(initialString:String):InputStream {
     return ByteArrayInputStream(initialString.toByteArray())
}

fun zipFolder(outPath:String)
{
    ZipUtil.pack(  File("$outPath/pdf"),   File("$outPath/pdf.zip"));
}