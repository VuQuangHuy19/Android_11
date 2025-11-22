package com.example.app6hu.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.example.app6hu.model.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class Export {
    public static File exportReportPDF(Context context, List<Transaction> list) {
        try {
            File path = new File(context.getExternalFilesDir(null), "BaoCao.pdf");

            PdfDocument pdf = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdf.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(16);

            int y = 50;
            canvas.drawText("BÁO CÁO GIAO DỊCH", 50, y, paint);
            y += 40;

            for (Transaction t : list) {
                canvas.drawText("- " + t.getCategory() + " | " + t.getAmount() + " VND | " + t.getDate(), 50, y, paint);
                y += 30;
            }

            pdf.finishPage(page);
            pdf.writeTo(new FileOutputStream(path));
            pdf.close();

            return path;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
