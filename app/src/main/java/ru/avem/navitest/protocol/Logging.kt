package ru.avem.navitest.protocol

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.github.mjdev.libaums.UsbMassStorageDevice
import io.realm.Realm
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.usermodel.charts.AxisPosition
import org.apache.poi.ss.usermodel.charts.ChartDataSource
import org.apache.poi.ss.usermodel.charts.DataSources
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFChart
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean
import ru.avem.navitest.R
import ru.avem.navitest.database.dot.ProtocolDot
import ru.avem.navitest.database.graph.ProtocolGraph
import ru.avem.navitest.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat

object Logging {
    private const val ACTION_USB_PERMISSION = "ru.avem.coilstestingfacility.USB_PERMISSION"
    fun preview(
        activity: Activity,
        protocolDot: ProtocolDot
    ) {
        if (requestPermission(activity)) {
            if (protocolDot != null) {
                SaveTask(2, activity).execute(protocolDot)
            } else {
                Toast.makeText(
                    activity,
                    "Выберите протокол из выпадающего списка",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                activity,
                "Ошибка доступа. Дайте разрешение на запись.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun requestPermission(activity: Activity): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                112
            )
            return false
        } else {
            val path =
                Environment.getExternalStorageDirectory().absolutePath + "/protocolDot"
            val storageDir = File(path)
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                return false
            }
        }
        return true
    }

    private fun writeWorkbookToMassStorage(
        protocolDot: ProtocolDot,
        context: Context
    ): String {
        val sdf =
            SimpleDateFormat("dd_MM(HH-mm-ss)", Utils.RU_LOCALE)
        var fileName =
            "protocolDot-" + sdf.format(System.currentTimeMillis()) + ".xlsx"
        val massStorageDevices =
            UsbMassStorageDevice.getMassStorageDevices(context)
        val currentDevice = massStorageDevices[0]
        try {
            currentDevice.init()
            val currentFS =
                currentDevice.partitions[0].fileSystem
            val root = currentFS.rootDirectory
            val file = root.createFile(fileName)
            val out =
                convertProtocolDotToWorkbook(protocolDot, context)
            file.write(0, ByteBuffer.wrap(out.toByteArray()))
            file.close()
            fileName = currentFS.volumeLabel + "/" + fileName
            currentDevice.close()
        } catch (e: IOException) {
            Log.e("TAG", "setup device error", e)
        }
        return fileName
    }

    @Throws(IOException::class)
    private fun convertProtocolDotToWorkbook(
        protocolDot: ProtocolDot,
        context: Context
    ): ByteArrayOutputStream {
        val res = context.resources
        val inputStream = res.openRawResource(R.raw.graph)
        val wb = XSSFWorkbook(inputStream)
        return wb.use { wb ->
            val sheet = wb.getSheetAt(0)
            for (i in 0..99) {
                val row = sheet.getRow(i)
                if (row != null) {
                    for (j in 0..9) {
                        val cell = row.getCell(j)
                        if (cell != null && cell.cellTypeEnum == CellType.STRING) {
                            when (cell.stringCellValue) {
                                "#PROTOCOL_NUMBER#" -> cell.setCellValue(protocolDot.id.toString())
                                "#DATE#" -> cell.setCellValue(protocolDot.dateDot)
                                "#TIME#" -> cell.setCellValue(protocolDot.timeDot)
                                "#RMS#" -> cell.setCellValue(protocolDot.rms)
                                "#AVR#" -> cell.setCellValue(protocolDot.avr)
                                "#AMP#" -> cell.setCellValue(protocolDot.amp)
                                "#FREQ#" -> cell.setCellValue(protocolDot.freq)
                                "#COEFAMP#" -> cell.setCellValue(protocolDot.coefamp)
                                "#COEFFORM#" -> cell.setCellValue(protocolDot.coefform)
                                else -> {
                                    if (cell.stringCellValue.contains("#")) {
                                        cell.setCellValue("")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val out = ByteArrayOutputStream()
            out.use { out ->
                wb.write(out)
            }
            out
        }
    }

    @Throws(IOException::class)
    private fun convertProtocolGraphToWorkbook(
        protocolGraph: ProtocolGraph,
        context: Context
    ): ByteArrayOutputStream {
        val res = context.resources
        val inputStream = res.openRawResource(R.raw.graph)
        val wb = XSSFWorkbook(inputStream)
        return wb.use { wb ->
            val sheet = wb.getSheetAt(0)
            for (i in 0..99) {
                val row = sheet.getRow(i)
                if (row != null) {
                    for (j in 0..9) {
                        val cell = row.getCell(j)
                        if (cell != null && cell.cellTypeEnum == CellType.STRING) {
                            when (cell.stringCellValue) {
                                "#PROTOCOL_NUMBER#" -> cell.setCellValue(protocolGraph.id.toString())
                                "#TYPEOFVALUE#" -> cell.setCellValue(protocolGraph.typeOfValue)
                                "#DATE#" -> cell.setCellValue(protocolGraph.date)
                                "#TIME#" -> cell.setCellValue(protocolGraph.time)
                                else -> {
                                    if (cell.stringCellValue.contains("#")) {
                                        cell.setCellValue("")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            fillParameters(wb, protocolGraph.values)
            drawLineChart(wb)
            val outStream = ByteArrayOutputStream()
            wb.write(outStream)
            outStream.close()
            val out = ByteArrayOutputStream()
            out.use { out ->
                wb.write(out)
            }
            out
        }
    }

    private fun drawLineChart(workbook: XSSFWorkbook) {
        val sheet = workbook.getSheet("Sheet1")
        val lastRowIndex = sheet.lastRowNum - 1
        val timeData =
            DataSources.fromNumericCellRange(sheet, CellRangeAddress(16, lastRowIndex, 0, 0))
        val valueData =
            DataSources.fromNumericCellRange(sheet, CellRangeAddress(16, lastRowIndex, 1, 1))

        var lineChart = createLineChart(sheet)
        drawLineChart(lineChart, timeData, valueData)
    }

    private fun drawLineChart(
        lineChart: XSSFChart,
        xAxisData: ChartDataSource<Number>,
        yAxisData: ChartDataSource<Number>
    ) {
        val data = lineChart.chartDataFactory.createLineChartData()

        val xAxis = lineChart.chartAxisFactory.createCategoryAxis(AxisPosition.BOTTOM)
        val yAxis = lineChart.createValueAxis(AxisPosition.LEFT)
        yAxis.crosses = org.apache.poi.ss.usermodel.charts.AxisCrosses.AUTO_ZERO

        val series = data.addSeries(xAxisData, yAxisData)
        series.setTitle("График")
        lineChart.plot(data, xAxis, yAxis)

        val plotArea = lineChart.ctChart.plotArea
        plotArea.lineChartArray[0].smooth
        val ctBool = CTBoolean.Factory.newInstance()
        ctBool.`val` = false
        plotArea.lineChartArray[0].smooth = ctBool
        for (series in plotArea.lineChartArray[0].serArray) {
            series.smooth = ctBool
        }
    }

    private fun createLineChart(sheet: XSSFSheet): XSSFChart {
        val drawing = sheet.createDrawingPatriarch()
        val anchor = drawing.createAnchor(0, 0, 0, 0, 3, 16, 6, 26)

        return drawing.createChart(anchor)
    }

    private fun fillParameters(wb: XSSFWorkbook, dots: String) {
        var values = dots.removePrefix("[").removePrefix("'").removeSuffix("]")
            .split(", ").map { it.replace(',', '.') }.map(String::toDouble)
        val sheet = wb.getSheetAt(0)
        var row: Row
        var cellStyle: XSSFCellStyle = generateStyles(wb) as XSSFCellStyle
        var rowNum = sheet.lastRowNum
        row = sheet.createRow(rowNum)
        var columnNum = 0
        for (i in values.indices) {
            columnNum = fillOneCell(row, columnNum, cellStyle, i)
            columnNum = fillOneCell(row, columnNum, cellStyle, values[i])
            row = sheet.createRow(++rowNum)
            columnNum = 0
        }
    }

    private fun fillOneCell(
        row: Row,
        columnNum: Int,
        cellStyle: XSSFCellStyle,
        points: Double
    ): Int {
        val cell: Cell = row.createCell(columnNum)
        cell.cellStyle = cellStyle
        cell.setCellValue(points)
        return columnNum + 1
    }

    private fun fillOneCell(row: Row, columnNum: Int, cellStyle: XSSFCellStyle, points: Int): Int {
        val cell: Cell = row.createCell(columnNum)
        cell.cellStyle = cellStyle
        cell.setCellValue(points.toString())
        return columnNum + 1
    }

    private fun generateStyles(wb: XSSFWorkbook): CellStyle {
        val headStyle: CellStyle = wb.createCellStyle()
        headStyle.wrapText = true
        headStyle.setBorderBottom(BorderStyle.THIN)
        headStyle.setBorderTop(BorderStyle.THIN)
        headStyle.setBorderLeft(BorderStyle.THIN)
        headStyle.setBorderRight(BorderStyle.THIN)
        headStyle.setAlignment(HorizontalAlignment.CENTER)
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER)
        return headStyle
    }

    private fun writeWorkbookToInternalStorage(
        protocol: ProtocolDot,
        context: Context
    ): String {
        clearDirectory(
            File(
                Environment.getExternalStorageDirectory().absolutePath + "/protocol"
            )
        )
        val sdf =
            SimpleDateFormat("dd_MM(HH-mm-ss)", Utils.RU_LOCALE)
        var fileName =
            "protocol-" + sdf.format(System.currentTimeMillis()) + ".xlsx"
        try {
            val out =
                convertProtocolDotToWorkbook(protocol, context)
            val file = File(
                Environment.getExternalStorageDirectory()
                    .absolutePath + "/protocol", fileName
            )
            val fileOut = FileOutputStream(file)
            out.writeTo(fileOut)
            fileName = Environment.getExternalStorageDirectory()
                .absolutePath + "/protocol/" + fileName
            out.close()
            fileOut.close()
        } catch (e: IOException) {
            Log.e("TAG", " error", e)
        }
        return fileName
    }

    private fun clearDirectory(directory: File) {
        for (child in directory.listFiles()) {
            child.delete()
        }
    }

    fun saveFileOnFlashMassStorage(
        context: Context,
        protocol: ProtocolDot
    ) {
        if (checkMassStorageConnection(context)) {
            SaveTask(1, context).execute(protocol)
        } else {
            AlertDialog.Builder(
                context
            )
                .setTitle("Нет подключения")
                .setCancelable(false)
                .setMessage("Подключите USB FLASH накопитель с файловой системой FAT32 и предоставьте доступ к нему")
                .setPositiveButton(
                    "ОК",
                    DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                .create()
                .show()
        }
    }

    private fun checkMassStorageConnection(context: Context): Boolean {
        val massStorageDevices =
            UsbMassStorageDevice.getMassStorageDevices(context)
        return if (massStorageDevices.size != 1) {
            false
        } else {
            val usbManager =
                context.getSystemService(Context.USB_SERVICE) as UsbManager
            if (usbManager.hasPermission(massStorageDevices[0].usbDevice)) {
                true
            } else {
                val pi = PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(ACTION_USB_PERMISSION),
                    0
                )
                usbManager.requestPermission(massStorageDevices[0].usbDevice, pi)
                false
            }
        }
    }

    fun isDeviceFlashMassStorage(device: UsbDevice): Boolean {
        val interfaceCount = device.interfaceCount
        for (i in 0 until interfaceCount) {
            val usbInterface = device.getInterface(i)
            val INTERFACE_SUBCLASS = 6
            val INTERFACE_PROTOCOL = 80
            if (usbInterface.interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE && usbInterface.interfaceSubclass == INTERFACE_SUBCLASS && usbInterface.interfaceProtocol == INTERFACE_PROTOCOL
            ) {
                return true
            }
        }
        return false
    }

    private class SaveTask internal constructor(
        private val mType: Int,
        private val mContext: Context
    ) :
        AsyncTask<ProtocolDot?, Void?, String?>() {
        private val dialog: ProgressDialog
        override fun onPreExecute() {
            super.onPreExecute()
            dialog.show()
        }

        override fun doInBackground(vararg params: ProtocolDot?): String? {
            var fileName: String? = null
            var protocolDot = params[0]!!
                if (mType == 1) {
                    fileName = writeWorkbookToMassStorage(
                        protocolDot,
                        mContext
                    )
                } else if (mType == 2) {
                    fileName = writeWorkbookToInternalStorage(
                        protocolDot,
                        mContext
                    )
                }
            return fileName
        }

        override fun onPostExecute(fileName: String?) {
            super.onPostExecute(fileName)
            dialog.dismiss()
            if (fileName != null) {
                if (mType == 1) {
                    Toast.makeText(mContext, "Сохранено в $fileName", Toast.LENGTH_SHORT).show()
                } else if (mType == 2) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    val file = File(fileName)
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val uri: Uri = FileProvider.getUriForFile(
                        mContext,
                        mContext.applicationContext.packageName + ".provider",
                        file
                    )
                    val mimeType =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            "xlsx"
                        )
                    intent.setDataAndType(uri, mimeType)
                    mContext.startActivity(intent)
                }
            } else {
                Toast.makeText(
                    mContext,
                    "Выберите протокол из выпадающего списка",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        init {
            dialog = ProgressDialog(mContext)
            if (mType == 1) {
                dialog.setMessage("Идёт сохранение...")
            } else if (mType == 2) {
                dialog.setMessage("Подождите...")
            }
            dialog.setCancelable(false)
        }
    }

}