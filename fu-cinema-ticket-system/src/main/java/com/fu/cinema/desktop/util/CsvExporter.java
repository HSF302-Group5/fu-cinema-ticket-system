package com.fu.cinema.desktop.util;

import com.fu.cinema.desktop.model.ReportBookingRow;
import com.fu.cinema.desktop.model.ReportData;
import com.fu.cinema.desktop.model.RevenueRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CsvExporter {

    private CsvExporter() {
    }

    public static void write(Path path, ReportData d) {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // 1. Ghi ký tự UTF-8 BOM (\uFEFF) giúp Excel tự nhận diện đúng bảng mã tiếng Việt
            w.write("\uFEFF");

            // 2. Tiêu đề kỳ báo cáo
            w.write("BÁO CÁO DOANH THU RẠP FU CINEMA");
            w.newLine();
            w.write("Khoảng thời gian: Từ ngày " + d.start() + " đến ngày " + d.end());
            w.newLine();
            w.newLine();

            // 3. Bảng Doanh thu theo phim
            w.write("DOANH THU THEO PHIM");
            w.newLine();
            w.write("Tên phim,Số vé bán ra,Doanh thu (VNĐ)");
            w.newLine();
            for (RevenueRow r : d.revenue()) {
                w.write(String.join(",", escape(r.title()), String.valueOf(r.tickets()), r.revenue().toPlainString()));
                w.newLine();
            }
            w.newLine();

            // 4. Bảng Chi tiết đơn đặt vé
            w.write("CHI TIẾT ĐƠN HÀNG HỢP LỆ (CONFIRMED)");
            w.newLine();
            w.write("Mã đơn,Thời gian đặt,Khách hàng,Phim,Tổng tiền (VNĐ)");
            w.newLine();
            for (ReportBookingRow b : d.bookings()) {
                w.write(String.join(",",
                        b.id().toString(),
                        b.bookingDate().toString(),
                        escape(b.customerName()),
                        escape(b.movieTitle()),
                        b.total().toPlainString()));
                w.newLine();
            }
            w.newLine();

            // 5. Dòng tổng kết
            w.write("TỔNG KẾT,%d đơn,%d vé,,%s".formatted(d.totalBookings(), d.totalTickets(), d.totalRevenue().toPlainString()));
            w.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Xử lý escape dấu phẩy, ngoặc kép chuẩn RFC 4180 */
    private static String escape(String v) {
        if (v == null) return "";
        return v.contains(",") || v.contains("\"") || v.contains("\n")
                ? "\"" + v.replace("\"", "\"\"") + "\""
                : v;
    }
}
