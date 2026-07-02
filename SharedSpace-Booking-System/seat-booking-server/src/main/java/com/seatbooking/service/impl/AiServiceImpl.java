package com.seatbooking.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seatbooking.client.DeepSeekClient;
import com.seatbooking.dto.BookingDTO;
import com.seatbooking.dto.ChatRequest;
import com.seatbooking.dto.ChatResponse;
import com.seatbooking.entity.Seat;
import com.seatbooking.entity.Space;
import com.seatbooking.entity.User;
import com.seatbooking.entity.Booking;
import com.seatbooking.service.AiService;
import com.seatbooking.service.BookingService;
import com.seatbooking.service.SeatService;
import com.seatbooking.service.SpaceService;
import com.seatbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String buildStudentSystemPrompt() {
        return "You are an intelligent assistant for a seat booking system. Your task is to understand the user's intent and return a JSON response.\n\n" +
                "User role: Student\n" +
                "Supported intents:\n" +
                "1. create_booking - Create a booking, params: spaceName, seatNumber, date(YYYY-MM-DD), startTime(HH:mm), endTime(HH:mm)\n" +
                "2. cancel_booking - Cancel a booking, params: bookingId or date+seatNumber\n" +
                "3. query_my_bookings - Query my bookings, params: status(optional, RESERVED/CHECKED_IN/CHECKED_OUT/CANCELLED/EXPIRED)\n" +
                "4. update_booking_time - Update booking time, params: bookingId or date+seatNumber, startTime, endTime\n" +
                "5. query_available_seats - Query available seats, params: spaceName, date(YYYY-MM-DD), startTime(HH:mm), endTime(HH:mm)\n\n" +
                "You must reply in JSON format:\n" +
                "{\"intent\": \"intent_name\", \"params\": {\"param_name\": \"param_value\"}, \"reply\": \"friendly reply to user\"}\n\n" +
                "如果无法识别意图，返回：\n" +
                "{\"intent\": \"unknown\", \"params\": {}, \"reply\": \"抱歉，我无法理解您的请求，请更具体地描述。\"}\n\n" +
                "Note:\n" +
                "- Date format must be YYYY-MM-DD, time format must be HH:mm\n" +
                "- If user says tomorrow or the day after tomorrow, calculate the specific date (today is " + LocalDate.now().toString() + ")\n" +
                "- If missing required params, remind user in reply\n" +
                "- Be friendly and concise\n";
    }

    private String buildTeacherSystemPrompt() {
        return "You are an intelligent assistant for a seat booking system. Your task is to understand the user's intent and return a JSON response.\n\n" +
                "User role: Teacher\n" +
                "You have a managed study room automatically assigned. You do NOT need to ask the user for the space name.\n" +
                "Supported intents:\n" +
                "1. query_space_bookings - Query booking info for managed study rooms, params: date(YYYY-MM-DD optional, default today), status(optional)\n" +
                "2. query_available_seats - Query available seats in managed spaces, params: date(YYYY-MM-DD optional, default today)\n\n" +
                "You must reply in JSON format:\n" +
                "{\"intent\": \"intent_name\", \"params\": {\"param_name\": \"param_value\"}, \"reply\": \"friendly reply to user\"}\n\n" +
                "If user asks to query available seats or bookings without specifying a space, just use the default managed space, do NOT ask for space name.\n" +
                "If user does not specify a date, default to today (" + LocalDate.now().toString() + ").\n" +
                "如果无法识别意图，返回：\n" +
                "{\"intent\": \"unknown\", \"params\": {}, \"reply\": \"抱歉，我无法理解您的请求，请更具体地描述。\"}\n\n" +
                "Note:\n" +
                "- Date format must be YYYY-MM-DD\n" +
                "- If user says today or tomorrow, calculate the specific date\n" +
                "- Be friendly and concise\n";
    }

    private String buildAdminSystemPrompt() {
        return "You are an intelligent assistant for a seat booking system. Your task is to understand the user's intent and return a JSON response.\n\n" +
                "User role: Administrator\n" +
                "Security restriction: For system security, you can only perform query operations, cannot create, update, or delete any data.\n" +
                "Supported intents:\n" +
                "1. query_my_bookings - Query all bookings, params: status(optional), date(YYYY-MM-DD optional)\n" +
                "2. query_available_seats - Query available seats, params: spaceName(optional), date(YYYY-MM-DD optional)\n" +
                "3. query_space_bookings - Query booking info for a space, params: spaceName(optional), date(YYYY-MM-DD optional)\n\n" +
                "You must reply in JSON format:\n" +
                "{\"intent\": \"intent_name\", \"params\": {\"param_name\": \"param_value\"}, \"reply\": \"friendly reply to user\"}\n\n" +
                "如果无法识别意图，返回：\n" +
                "{\"intent\": \"unknown\", \"params\": {}, \"reply\": \"抱歉，我无法理解您的请求，请更具体地描述。\"}\n\n" +
                "Note:\n" +
                "- Date format must be YYYY-MM-DD\n" +
                "- If user says today or tomorrow, calculate the specific date (today is " + LocalDate.now().toString() + ")\n" +
                "- Be friendly and concise\n";
    }

    @Override
    public ChatResponse chat(Long userId, String role, ChatRequest request) {
        String systemPrompt;
        if ("TEACHER".equals(role)) {
            systemPrompt = buildTeacherSystemPrompt();
        } else if ("ADMIN".equals(role)) {
            systemPrompt = buildAdminSystemPrompt();
        } else {
            systemPrompt = buildStudentSystemPrompt();
        }

        String aiResponse = deepSeekClient.chat(systemPrompt, request.getMessage(), request.getHistory());

        try {
            JsonNode jsonNode = objectMapper.readTree(aiResponse);
            String intent = jsonNode.has("intent") ? jsonNode.get("intent").asText() : "unknown";
            JsonNode paramsNode = jsonNode.has("params") ? jsonNode.get("params") : objectMapper.createObjectNode();
            String reply = jsonNode.has("reply") ? jsonNode.get("reply").asText() : aiResponse;

            ChatResponse response = new ChatResponse();
            response.setIntent(intent);
            response.setReply(reply);

            if ("unknown".equals(intent)) {
                response.setSuccess(false);
                return response;
            }

            Map<String, Object> params = new HashMap<>();
            paramsNode.fields().forEachRemaining(entry -> params.put(entry.getKey(), entry.getValue().asText()));

            Object data = executeIntent(userId, role, intent, params);
            response.setSuccess(true);
            response.setData(data);

            if (data != null) {
                String resultReply = buildResultReply(intent, data, reply);
                response.setReply(resultReply);
            }

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            if ("TEACHER".equals(role)) {
                ChatResponse response = new ChatResponse();
                response.setIntent("query_available_seats");
                response.setSuccess(true);
                Map<String, Object> params = new HashMap<>();
                Object data = executeIntent(userId, role, "query_available_seats", params);
                response.setData(data);
                if (data != null) {
                    String resultReply = buildResultReply("query_available_seats", data, "");
                    response.setReply(resultReply);
                }
                return response;
            }
            ChatResponse response = new ChatResponse();
            response.setIntent("unknown");
            response.setReply(aiResponse);
            response.setSuccess(false);
            return response;
        }
    }

    private Object executeIntent(Long userId, String role, String intent, Map<String, Object> params) {
        try {
            switch (intent) {
                case "create_booking":
                    return createBooking(userId, params);
                case "cancel_booking":
                    return cancelBooking(userId, params);
                case "query_my_bookings":
                    return queryMyBookings(userId, params);
                case "update_booking_time":
                    return updateBookingTime(userId, params);
                case "query_available_seats":
                    return queryAvailableSeats(userId, role, params);
                case "query_space_bookings":
                    return querySpaceBookings(userId, role, params);
                default:
                    return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Booking createBooking(Long userId, Map<String, Object> params) {
        String spaceName = (String) params.get("spaceName");
        String seatNumber = (String) params.get("seatNumber");
        String dateStr = (String) params.get("date");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");

        if (spaceName == null || seatNumber == null || dateStr == null || startTime == null || endTime == null) {
            throw new RuntimeException("缺少必要参数：spaceName, seatNumber, date, startTime, endTime");
        }

        List<Space> spaces = spaceService.getAllSpaces();
        Space space = spaces.stream()
                .filter(s -> s.getName().contains(spaceName) || spaceName.contains(s.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("空间不存在：" + spaceName));

        List<Seat> seats = seatService.getSeatsBySpaceId(space.getId());
        Seat seat = seats.stream()
                .filter(s -> s.getSeatNumber().equals(seatNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("座位不存在：" + seatNumber));

        LocalDate date = LocalDate.parse(dateStr);
        return bookingService.createBooking(userId, seat.getId(), space.getId(), date, startTime, endTime);
    }

    private Boolean cancelBooking(Long userId, Map<String, Object> params) {
        String bookingIdStr = (String) params.get("bookingId");
        String dateStr = (String) params.get("date");
        String seatNumber = (String) params.get("seatNumber");
        String reason = (String) params.getOrDefault("reason", "用户取消");

        if (bookingIdStr != null) {
            Long bookingId = Long.parseLong(bookingIdStr);
            return bookingService.cancelBooking(bookingId, userId, reason);
        }

        if (dateStr != null && seatNumber != null) {
            LocalDate date = LocalDate.parse(dateStr);
            List<BookingDTO> bookings = bookingService.getBookingsByUserAndDate(userId, date);
            BookingDTO booking = bookings.stream()
                    .filter(b -> b.getSeatNumber().equals(seatNumber) && "RESERVED".equals(b.getStatus()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到该日期和座位的预约"));
            return bookingService.cancelBooking(booking.getId(), userId, reason);
        }

        throw new RuntimeException("缺少必要参数：bookingId 或 date+seatNumber");
    }

    private List<BookingDTO> queryMyBookings(Long userId, Map<String, Object> params) {
        String status = (String) params.get("status");
        return bookingService.getBookingList(userId, status, 1, 100).getRecords();
    }

    private Boolean updateBookingTime(Long userId, Map<String, Object> params) {
        String bookingIdStr = (String) params.get("bookingId");
        String dateStr = (String) params.get("date");
        String seatNumber = (String) params.get("seatNumber");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");

        if (startTime == null || endTime == null) {
            throw new RuntimeException("缺少必要参数：startTime, endTime");
        }

        Long bookingId;
        if (bookingIdStr != null) {
            bookingId = Long.parseLong(bookingIdStr);
        } else if (dateStr != null && seatNumber != null) {
            LocalDate date = LocalDate.parse(dateStr);
            List<BookingDTO> bookings = bookingService.getBookingsByUserAndDate(userId, date);
            BookingDTO booking = bookings.stream()
                    .filter(b -> b.getSeatNumber().equals(seatNumber) && "RESERVED".equals(b.getStatus()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到该日期和座位的预约"));
            bookingId = booking.getId();
        } else {
            throw new RuntimeException("缺少必要参数：bookingId 或 date+seatNumber");
        }

        bookingService.updateBookingTime(bookingId, userId, startTime, endTime);
        return true;
    }

    private Object queryAvailableSeats(Long userId, String role, Map<String, Object> params) {
        String spaceName = (String) params.get("spaceName");
        String dateStr = (String) params.get("date");
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");

        List<Space> spaces = spaceService.getAllSpaces();
        
        if ("TEACHER".equals(role)) {
            User teacher = userService.getById(userId);
            if (teacher.getManagedSpaceId() != null) {
                spaces = spaces.stream()
                        .filter(s -> s.getId().equals(teacher.getManagedSpaceId()))
                        .collect(Collectors.toList());
            }
        }

        Space space = null;
        if (spaceName == null) {
            if ("TEACHER".equals(role) && !spaces.isEmpty()) {
                space = spaces.get(0);
            } else {
                return spaces.stream().map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("spaceId", s.getId());
                    map.put("spaceName", s.getName());
                    map.put("availableSeats", seatService.getAvailableSeats(s.getId()).size());
                    return map;
                }).collect(Collectors.toList());
            }
        } else {
            space = spaces.stream()
                    .filter(s -> s.getName().contains(spaceName) || spaceName.contains(s.getName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("空间不存在：" + spaceName));
        }

        LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
        List<BookingDTO> bookings = bookingService.getBookingsBySpaceAndDate(space.getId(), date);

        List<Map<String, Object>> availableSeatList = new ArrayList<>();
        int usedCount = 0;
        
        for (Seat seat : seatService.getSeatsBySpaceId(space.getId())) {
            if (seat.getStatus() != 1) {
                usedCount++;
                continue;
            }
            
            List<String> bookedTimes = new ArrayList<>();
            for (BookingDTO b : bookings) {
                if (b.getSeatId().equals(seat.getId()) && ("RESERVED".equals(b.getStatus()) || "CHECKED_IN".equals(b.getStatus()))) {
                    bookedTimes.add(b.getStartTime() + "-" + b.getEndTime());
                }
            }
            
            if (!bookedTimes.isEmpty()) {
                usedCount++;
            }
            
            List<String> availableTimeSlots = calculateAvailableTimeSlots(bookedTimes);
            
            Map<String, Object> map = new HashMap<>();
            map.put("seatNumber", seat.getSeatNumber());
            map.put("availableTimeSlots", availableTimeSlots);
            map.put("isAvailable", availableTimeSlots.size() > 0);
            availableSeatList.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("spaceName", space.getName());
        result.put("totalSeats", seatService.getSeatsBySpaceId(space.getId()).size());
        result.put("usedSeats", usedCount);
        result.put("availableSeats", seatService.getSeatsBySpaceId(space.getId()).size() - usedCount);
        result.put("date", date.toString());
        result.put("seats", availableSeatList);
        
        return result;
    }

    private List<String> calculateAvailableTimeSlots(List<String> bookedTimes) {
        List<String> allSlots = new ArrayList<>();
        for (int i = 8; i < 22; i++) {
            allSlots.add(String.format("%02d:00-%02d:00", i, i + 1));
        }
        
        for (String booked : bookedTimes) {
            allSlots.remove(booked);
        }
        
        return allSlots;
    }

    private List<BookingDTO> querySpaceBookings(Long userId, String role, Map<String, Object> params) {
        String spaceName = (String) params.get("spaceName");
        String dateStr = (String) params.get("date");
        String status = (String) params.get("status");

        List<Space> spaces = spaceService.getAllSpaces();
        Long finalManagedSpaceId = null;

        if ("TEACHER".equals(role)) {
            User teacher = userService.getById(userId);
            if (teacher.getManagedSpaceId() != null) {
                finalManagedSpaceId = teacher.getManagedSpaceId();
                final Long tempId = finalManagedSpaceId;
                spaces = spaces.stream()
                        .filter(s -> s.getId().equals(tempId))
                        .collect(Collectors.toList());
            }
        }

        if (spaceName != null) {
            Space space = spaces.stream()
                    .filter(s -> s.getName().contains(spaceName) || spaceName.contains(s.getName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("空间不存在：" + spaceName));

            if (dateStr != null) {
                LocalDate date = LocalDate.parse(dateStr);
                return bookingService.getBookingsBySpaceAndDate(space.getId(), date);
            }
            return bookingService.getBookingsBySpaceAndDate(space.getId(), null);
        }

        if (finalManagedSpaceId != null) {
            LocalDate bookingDate = dateStr != null ? LocalDate.parse(dateStr) : null;
            return bookingService.getBookingsBySpaceAndDate(finalManagedSpaceId, bookingDate);
        }

        LocalDate bookingDate = dateStr != null ? LocalDate.parse(dateStr) : null;
        return bookingService.getAllBookings(spaceName, null, bookingDate, status, 1, 100).getRecords();
    }

    private String buildResultReply(String intent, Object data, String originalReply) {
        switch (intent) {
            case "create_booking":
                Booking booking = (Booking) data;
                return "预约创建成功！ID: " + booking.getId() + ", 座位: " + booking.getSeatId() + ", 日期: " + booking.getBookingDate() + ", 时间: " + booking.getStartTime() + "-" + booking.getEndTime();
            case "cancel_booking":
                return "预约取消成功！";
            case "query_my_bookings":
                List<BookingDTO> bookings = (List<BookingDTO>) data;
                if (bookings.isEmpty()) {
                    return "您暂无预约记录。";
                }
                StringBuilder sb = new StringBuilder();
                sb.append("<table style='border-collapse: collapse; width: 100%; font-size: 13px;'>");
                sb.append("<thead><tr style='background: #f5f7fa;'>");
                sb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>空间名称</th>");
                sb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>座位号</th>");
                sb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>日期</th>");
                sb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>时间</th>");
                sb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>状态</th>");
                sb.append("</tr></thead><tbody>");
                for (BookingDTO b : bookings) {
                    sb.append("<tr>");
                    sb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(b.getSpaceName()).append("</td>");
                    sb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(b.getSeatNumber()).append("</td>");
                    sb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(b.getBookingDate()).append("</td>");
                    sb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(b.getStartTime()).append("-").append(b.getEndTime()).append("</td>");
                    sb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(getStatusText(b.getStatus())).append("</td>");
                    sb.append("</tr>");
                }
                sb.append("</tbody></table>");
                return "您共有 " + bookings.size() + " 条预约记录：\n" + sb.toString();
            case "update_booking_time":
                return "预约时间修改成功！";
            case "query_available_seats":
                if (data instanceof List) {
                    List<Map<String, Object>> seatsList = (List<Map<String, Object>>) data;
                    if (seatsList.isEmpty()) {
                        return "暂无可用座位。";
                    }
                    StringBuilder seatSb = new StringBuilder();
                    seatSb.append("<table style='border-collapse: collapse; width: 100%; font-size: 13px;'>");
                    seatSb.append("<thead><tr style='background: #f5f7fa;'>");
                    seatSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>空间名称</th>");
                    seatSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>可用座位数</th>");
                    seatSb.append("</tr></thead><tbody>");
                    for (Map<String, Object> s : seatsList) {
                        seatSb.append("<tr>");
                        seatSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(s.get("spaceName")).append("</td>");
                        seatSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(s.get("availableSeats")).append("</td>");
                        seatSb.append("</tr>");
                    }
                    seatSb.append("</tbody></table>");
                    return "可用座位：\n" + seatSb.toString();
                } else {
                    Map<String, Object> result = (Map<String, Object>) data;
                    String spaceName = (String) result.get("spaceName");
                    Integer totalSeats = (Integer) result.get("totalSeats");
                    Integer usedSeats = (Integer) result.get("usedSeats");
                    Integer availableSeats = (Integer) result.get("availableSeats");
                    String date = (String) result.get("date");
                    List<Map<String, Object>> seats = (List<Map<String, Object>>) result.get("seats");
                    
                    StringBuilder seatSb = new StringBuilder();
                    seatSb.append("<table style='border-collapse: collapse; width: 100%; font-size: 13px;'>");
                    seatSb.append("<thead><tr style='background: #f5f7fa;'>");
                    seatSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>座位号</th>");
                    seatSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>可预约时间段</th>");
                    seatSb.append("</tr></thead><tbody>");
                    for (Map<String, Object> s : seats) {
                        Boolean isAvailable = (Boolean) s.get("isAvailable");
                        if (!isAvailable) continue;
                        seatSb.append("<tr>");
                        seatSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(s.get("seatNumber")).append("</td>");
                        List<String> timeSlots = (List<String>) s.get("availableTimeSlots");
                        seatSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(String.join(", ", timeSlots)).append("</td>");
                        seatSb.append("</tr>");
                    }
                    seatSb.append("</tbody></table>");
                    return spaceName + "（" + date + "）总共 " + totalSeats + " 个座位，其中 " + usedSeats + " 个已被预约，剩余 " + availableSeats + " 个可用。\n可用座位信息如下：\n" + seatSb.toString();
                }
            case "query_space_bookings":
                List<BookingDTO> spaceBookings = (List<BookingDTO>) data;
                if (spaceBookings.isEmpty()) {
                    return "该空间暂无预约记录。";
                }
                StringBuilder spaceSb = new StringBuilder();
                spaceSb.append("<table style='border-collapse: collapse; width: 100%; font-size: 13px;'>");
                spaceSb.append("<thead><tr style='background: #f5f7fa;'>");
                spaceSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>用户名</th>");
                spaceSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>空间名称</th>");
                spaceSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>座位号</th>");
                spaceSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>日期</th>");
                spaceSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>时间</th>");
                spaceSb.append("<th style='border: 1px solid #e4e7ed; padding: 8px; text-align: left;'>状态</th>");
                spaceSb.append("</tr></thead><tbody>");
                for (BookingDTO b : spaceBookings) {
                    spaceSb.append("<tr>");
                    spaceSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(b.getUserName()).append("</td>");
                    spaceSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(b.getSpaceName()).append("</td>");
                    spaceSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(b.getSeatNumber()).append("</td>");
                    spaceSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(b.getBookingDate()).append("</td>");
                    spaceSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(b.getStartTime()).append("-").append(b.getEndTime()).append("</td>");
                    spaceSb.append("<td style='border: 1px solid #e4e7ed; padding: 8px;'>").append(getStatusText(b.getStatus())).append("</td>");
                    spaceSb.append("</tr>");
                }
                spaceSb.append("</tbody></table>");
                return "共找到 " + spaceBookings.size() + " 条预约记录：\n" + spaceSb.toString();
            default:
                return originalReply;
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "RESERVED": return "已预约";
            case "CHECKED_IN": return "已签到";
            case "CHECKED_OUT": return "已签退";
            case "CANCELLED": return "已取消";
            case "EXPIRED": return "已过期";
            default: return status;
        }
    }
}