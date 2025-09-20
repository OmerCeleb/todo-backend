package com.todoapp.dto;

/**
 * Data Transfer Object for Todo statistics and analytics.
 * Provides summary information about todo counts and status.
 */
public class TodoStatsDTO {

    private long total;
    private long completed;
    private long active;
    private long overdue;

    /**
     * Default constructor
     */
    public TodoStatsDTO() {}

    /**
     * Constructor with all statistics
     * @param total Total number of todos
     * @param completed Number of completed todos
     * @param active Number of active (incomplete) todos
     * @param overdue Number of overdue todos
     */
    public TodoStatsDTO(long total, long completed, long active, long overdue) {
        this.total = total;
        this.completed = completed;
        this.active = active;
        this.overdue = overdue;
    }

    // Getters and Setters

    /**
     * Get total number of todos
     * @return total todo count
     */
    public long getTotal() {
        return total;
    }

    /**
     * Set total number of todos
     * @param total total todo count
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * Get number of completed todos
     * @return completed todo count
     */
    public long getCompleted() {
        return completed;
    }

    /**
     * Set number of completed todos
     * @param completed completed todo count
     */
    public void setCompleted(long completed) {
        this.completed = completed;
    }

    /**
     * Get number of active (incomplete) todos
     * @return active todo count
     */
    public long getActive() {
        return active;
    }

    /**
     * Set number of active (incomplete) todos
     * @param active active todo count
     */
    public void setActive(long active) {
        this.active = active;
    }

    /**
     * Get number of overdue todos
     * @return overdue todo count
     */
    public long getOverdue() {
        return overdue;
    }

    /**
     * Set number of overdue todos
     * @param overdue overdue todo count
     */
    public void setOverdue(long overdue) {
        this.overdue = overdue;
    }

    /**
     * Calculate completion percentage
     * @return completion percentage (0-100)
     */
    public double getCompletionPercentage() {
        if (total == 0) return 0.0;
        return (double) completed / total * 100.0;
    }

    @Override
    public String toString() {
        return "TodoStatsDTO{" +
                "total=" + total +
                ", completed=" + completed +
                ", active=" + active +
                ", overdue=" + overdue +
                ", completionPercentage=" + String.format("%.1f", getCompletionPercentage()) + "%" +
                '}';
    }
}