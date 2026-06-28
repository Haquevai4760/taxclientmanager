package com.taxclientmanager.app.utils

import android.content.Context
import com.taxclientmanager.app.data.model.Client
import com.taxclientmanager.app.data.model.ServiceRecord
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.io.OutputStreamWriter

object ExportUtils {

    fun exportClientsToCSV(clients: List<Client>, outputStream: OutputStream): Boolean {
        return try {
            val writer = OutputStreamWriter(outputStream)
            val printer = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("TIN", "Name", "Mobile", "Address", "Profession", "Status"))
            
            for (client in clients) {
                printer.printRecord(
                    client.tinNumber,
                    client.fullName,
                    client.mobileNumber,
                    client.address,
                    client.profession,
                    if (client.isActive) "Active" else "Inactive"
                )
            }
            printer.flush()
            printer.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportServicesToCSV(services: List<ServiceRecord>, outputStream: OutputStream): Boolean {
        return try {
            val writer = OutputStreamWriter(outputStream)
            val printer = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("TIN", "Service Type", "Income Year", "Charge", "Paid", "Balance", "Date"))
            
            for (service in services) {
                printer.printRecord(
                    service.tinNumber,
                    service.serviceType.displayName,
                    service.incomeYear,
                    service.serviceCharge,
                    service.paidAmount,
                    service.serviceCharge - service.paidAmount,
                    DateUtils.formatDate(service.serviceDate)
                )
            }
            printer.flush()
            printer.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportClientsToExcel(clients: List<Client>, outputStream: OutputStream): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Clients")
            
            val headerRow = sheet.createRow(0)
            val headers = listOf("TIN", "Name", "Mobile", "Address", "Profession", "Status")
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }
            
            clients.forEachIndexed { index, client ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(client.tinNumber)
                row.createCell(1).setCellValue(client.fullName)
                row.createCell(2).setCellValue(client.mobileNumber)
                row.createCell(3).setCellValue(client.address)
                row.createCell(4).setCellValue(client.profession)
                row.createCell(5).setCellValue(if (client.isActive) "Active" else "Inactive")
            }
            
            workbook.write(outputStream)
            workbook.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
