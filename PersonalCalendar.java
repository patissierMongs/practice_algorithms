import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.io.*;

public class PersonalCalendar extends JFrame {
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private JButton prevButton, nextButton, todayButton;
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private Map<LocalDate, List<String>> events;
    private JTextArea eventArea;
    private static final String DATA_FILE = "calendar_events.dat";
    
    public PersonalCalendar() {
        setTitle("개인 캘린더");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        currentDate = LocalDate.now();
        selectedDate = currentDate;
        events = new HashMap<>();
        
        initComponents();
        loadEvents();
        updateCalendar();
        
        setSize(900, 700);
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        // 상단 네비게이션 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel navPanel = new JPanel(new FlowLayout());
        prevButton = new JButton("◀");
        nextButton = new JButton("▶");
        todayButton = new JButton("오늘");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        
        prevButton.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            updateCalendar();
        });
        
        nextButton.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            updateCalendar();
        });
        
        todayButton.addActionListener(e -> {
            currentDate = LocalDate.now();
            selectedDate = currentDate;
            updateCalendar();
        });
        
        navPanel.add(prevButton);
        navPanel.add(todayButton);
        navPanel.add(nextButton);
        
        topPanel.add(monthLabel, BorderLayout.NORTH);
        topPanel.add(navPanel, BorderLayout.CENTER);
        
        // 중앙 캘린더 패널
        calendarPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 우측 이벤트 패널
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        
        JLabel eventLabel = new JLabel("일정 관리");
        eventLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        eventLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        eventArea = new JTextArea();
        eventArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(eventArea);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("일정 추가");
        JButton deleteButton = new JButton("일정 삭제");
        JButton saveButton = new JButton("저장");
        
        addButton.addActionListener(e -> addEvent());
        deleteButton.addActionListener(e -> deleteEvent());
        saveButton.addActionListener(e -> saveEvents());
        
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        
        rightPanel.add(eventLabel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 메인 프레임에 추가
        add(topPanel, BorderLayout.NORTH);
        add(calendarPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }
    
    private void updateCalendar() {
        calendarPanel.removeAll();
        
        // 월 레이블 업데이트
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월");
        monthLabel.setText(currentDate.format(formatter));
        
        // 요일 헤더 추가
        String[] weekDays = {"일", "월", "화", "수", "목", "금", "토"};
        for (int i = 0; i < weekDays.length; i++) {
            JLabel label = new JLabel(weekDays[i], SwingConstants.CENTER);
            label.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            if (i == 0) {
                label.setForeground(Color.RED);
            } else if (i == 6) {
                label.setForeground(Color.BLUE);
            }
            calendarPanel.add(label);
        }
        
        // 달력 날짜 추가
        LocalDate firstDay = currentDate.withDayOfMonth(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentDate.lengthOfMonth();
        
        // 빈 칸 추가
        for (int i = 0; i < dayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        // 날짜 버튼 추가
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentDate.withDayOfMonth(day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            
            // 오늘 날짜 강조
            if (date.equals(LocalDate.now())) {
                dayButton.setBackground(new Color(255, 255, 200));
            }
            
            // 선택된 날짜 강조
            if (date.equals(selectedDate)) {
                dayButton.setBackground(new Color(200, 220, 255));
            }
            
            // 일정이 있는 날짜 표시
            if (events.containsKey(date) && !events.get(date).isEmpty()) {
                dayButton.setForeground(new Color(0, 150, 0));
                dayButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            }
            
            // 주말 색상
            int currentDayOfWeek = date.getDayOfWeek().getValue() % 7;
            if (currentDayOfWeek == 0) {
                dayButton.setForeground(Color.RED);
            } else if (currentDayOfWeek == 6) {
                dayButton.setForeground(Color.BLUE);
            }
            
            dayButton.addActionListener(e -> {
                selectedDate = date;
                updateCalendar();
                displayEvents();
            });
            
            calendarPanel.add(dayButton);
        }
        
        // 남은 빈 칸 추가
        int totalCells = dayOfWeek + daysInMonth;
        int remainingCells = 42 - totalCells;
        for (int i = 0; i < remainingCells; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
        displayEvents();
    }
    
    private void displayEvents() {
        eventArea.setText("");
        eventArea.append("선택된 날짜: " + selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) + "\n");
        eventArea.append("=====================================\n\n");
        
        List<String> dayEvents = events.get(selectedDate);
        if (dayEvents != null && !dayEvents.isEmpty()) {
            for (int i = 0; i < dayEvents.size(); i++) {
                eventArea.append((i + 1) + ". " + dayEvents.get(i) + "\n");
            }
        } else {
            eventArea.append("일정이 없습니다.\n");
        }
    }
    
    private void addEvent() {
        String event = JOptionPane.showInputDialog(this, 
            selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) + "의 일정을 입력하세요:");
        
        if (event != null && !event.trim().isEmpty()) {
            events.computeIfAbsent(selectedDate, k -> new ArrayList<>()).add(event);
            updateCalendar();
            displayEvents();
        }
    }
    
    private void deleteEvent() {
        List<String> dayEvents = events.get(selectedDate);
        if (dayEvents == null || dayEvents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "삭제할 일정이 없습니다.");
            return;
        }
        
        String[] options = dayEvents.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(this,
            "삭제할 일정을 선택하세요:",
            "일정 삭제",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (selected != null) {
            dayEvents.remove(selected);
            if (dayEvents.isEmpty()) {
                events.remove(selectedDate);
            }
            updateCalendar();
            displayEvents();
        }
    }
    
    private void saveEvents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(events);
            JOptionPane.showMessageDialog(this, "일정이 저장되었습니다.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "저장 실패: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadEvents() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                events = (Map<LocalDate, List<String>>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "데이터 로드 실패: " + e.getMessage());
                events = new HashMap<>();
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            PersonalCalendar calendar = new PersonalCalendar();
            calendar.setVisible(true);
        });
    }
}
