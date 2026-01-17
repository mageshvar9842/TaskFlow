#!/usr/bin/env python3
"""
Example usage of TaskFlow
"""

from taskflow import TaskFlow


def main():
    """Demonstrate TaskFlow functionality."""
    print("=" * 60)
    print("TaskFlow Example Usage")
    print("=" * 60)
    print()
    
    # Initialize TaskFlow
    tf = TaskFlow("example_tasks.json")
    
    # Clear existing tasks for demo
    for task in tf.get_all_tasks():
        tf.delete_task(task.id)
    
    # Create some tasks
    print("1. Creating tasks...")
    task1 = tf.create_task(
        "Implement user authentication",
        "Add JWT-based authentication system",
        "high"
    )
    print(f"   Created: {task1}")
    
    task2 = tf.create_task(
        "Write documentation",
        "Create API documentation and user guide",
        "medium"
    )
    print(f"   Created: {task2}")
    
    task3 = tf.create_task(
        "Fix bug in login page",
        "Users can't login with special characters in password",
        "high"
    )
    print(f"   Created: {task3}")
    
    task4 = tf.create_task(
        "Update dependencies",
        "Update all npm packages to latest versions",
        "low"
    )
    print(f"   Created: {task4}")
    print()
    
    # List all tasks
    print("2. Listing all tasks...")
    for task in tf.get_all_tasks():
        print(f"   {task}")
    print()
    
    # Update task status
    print("3. Updating task statuses...")
    tf.update_task(task1.id, status="in_progress")
    print(f"   Task {task1.id} status updated to 'in_progress'")
    
    tf.update_task(task3.id, status="completed")
    print(f"   Task {task3.id} status updated to 'completed'")
    print()
    
    # View tasks by status
    print("4. Viewing tasks by status...")
    print("   Pending tasks:")
    for task in tf.get_tasks_by_status("pending"):
        print(f"     {task}")
    
    print("   In Progress tasks:")
    for task in tf.get_tasks_by_status("in_progress"):
        print(f"     {task}")
    
    print("   Completed tasks:")
    for task in tf.get_tasks_by_status("completed"):
        print(f"     {task}")
    print()
    
    # View tasks by priority
    print("5. Viewing high priority tasks...")
    for task in tf.get_tasks_by_priority("high"):
        print(f"   {task}")
    print()
    
    # Get task details
    print("6. Getting detailed task information...")
    task = tf.get_task(task2.id)
    if task:
        print(f"   Task ID: {task.id}")
        print(f"   Title: {task.title}")
        print(f"   Description: {task.description}")
        print(f"   Priority: {task.priority}")
        print(f"   Status: {task.status}")
        print(f"   Created: {task.created_at}")
        print(f"   Updated: {task.updated_at}")
    print()
    
    # Update task details
    print("7. Updating task details...")
    tf.update_task(
        task2.id,
        title="Write comprehensive documentation",
        description="Create API docs, user guide, and tutorial videos",
        priority="high"
    )
    print(f"   Task {task2.id} updated")
    updated_task = tf.get_task(task2.id)
    print(f"   {updated_task}")
    print()
    
    # Get statistics
    print("8. Task Statistics...")
    stats = tf.get_statistics()
    print(f"   Total Tasks: {stats['total_tasks']}")
    print("   By Status:")
    for status, count in stats['by_status'].items():
        print(f"     {status}: {count}")
    print("   By Priority:")
    for priority, count in stats['by_priority'].items():
        print(f"     {priority}: {count}")
    print()
    
    # Delete a task
    print("9. Deleting a task...")
    if tf.delete_task(task4.id):
        print(f"   Task {task4.id} deleted successfully")
    print(f"   Remaining tasks: {len(tf.get_all_tasks())}")
    print()
    
    print("=" * 60)
    print("Example completed!")
    print("=" * 60)


if __name__ == "__main__":
    main()
