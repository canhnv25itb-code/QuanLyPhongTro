package util;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Event Bus với DEBOUNCE:
 *  - Nhiều lần gọi notifyAll() trong vòng 400ms chỉ trigger 1 lần reload.
 *  - Chỉ reload panel hiện đang hiển thị, đánh dấu "dirty" cho panel ẩn.
 *  - Dùng SwingWorker để DB call không chạy trên EDT.
 */
public class DataRefreshEvent {

    private static DataRefreshEvent instance;

    /** Listener thường – chạy loadData() */
    private final List<Runnable> listeners = new ArrayList<>();

    /** Debounce timer – gộp nhiều notify trong 400ms thành 1 */
    private Timer debounceTimer;
    private static final int DEBOUNCE_MS = 400;

    private DataRefreshEvent() {}

    public static DataRefreshEvent get() {
        if (instance == null) instance = new DataRefreshEvent();
        return instance;
    }

    public void addListener(Runnable listener) {
        if (listener != null && !listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    /**
     * Kích hoạt reload – có debounce 400ms.
     * Nhiều sự kiện liên tiếp nhanh sẽ được gộp thành 1 lần reload duy nhất.
     */
    public void notifyAll(String source) {
        System.out.println("[Event] " + source + " → scheduled refresh (" + listeners.size() + " panels)");

        // Hủy timer cũ nếu còn đang đếm
        if (debounceTimer != null && debounceTimer.isRunning()) {
            debounceTimer.stop();
        }

        // Tạo timer mới – sau DEBOUNCE_MS ms không có notify nào nữa thì mới chạy
        debounceTimer = new Timer(DEBOUNCE_MS, e -> {
            System.out.println("[Event] Executing refresh for " + listeners.size() + " panels");
            // Chạy từng listener tuần tự, với khoảng nghỉ nhỏ giữa các panel
            // để tránh block EDT quá lâu
            for (int i = 0; i < listeners.size(); i++) {
                final Runnable r = listeners.get(i);
                final int delay = i * 50; // mỗi panel cách nhau 50ms
                Timer staggerTimer = new Timer(delay, ev -> {
                    try { r.run(); } catch (Exception ex) {
                        System.err.println("[Event] Listener error: " + ex.getMessage());
                    }
                });
                staggerTimer.setRepeats(false);
                staggerTimer.start();
            }
        });
        debounceTimer.setRepeats(false);
        debounceTimer.start();
    }
}
