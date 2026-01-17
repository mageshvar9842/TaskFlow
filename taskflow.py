#!/usr/bin/env python3
"""
TaskFlow - A Simple Task Management System
"""

import json
import os
from datetime import datetime
from typing import List, Optional, Dict


class Task:
    """Represents a single task in the TaskFlow system."""
    
    def __init__(self, task_id: int, title: str, description: str = "", 
                 priority: str = "medium", status: str = "pending"):
        self.id = task_id
        self.title = title
        self.description = description
        self.priority = priority  # low, medium, high
        self.status = status  # pending, in_progress, completed, cancelled
        self.created_at = datetime.now().isoformat()
        self.updated_at = datetime.now().isoformat()
    
    def to_dict(self) -> Dict:
        """Convert task to dictionary."""
        return {
            'id': self.id,
            'title': self.title,
            'description': self.description,
            'priority': self.priority,
            'status': self.status,
            'created_at': self.created_at,
            'updated_at': self.updated_at
        }
    
    @classmethod
    def from_dict(cls, data: Dict) -> 'Task':
        """Create task from dictionary."""
        task = cls(
            task_id=data['id'],
            title=data['title'],
            description=data.get('description', ''),
            priority=data.get('priority', 'medium'),
            status=data.get('status', 'pending')
        )
        task.created_at = data.get('created_at', task.created_at)
        task.updated_at = data.get('updated_at', task.updated_at)
        return task
    
    def update_status(self, new_status: str):
        """Update task status."""
        valid_statuses = ['pending', 'in_progress', 'completed', 'cancelled']
        if new_status in valid_statuses:
            self.status = new_status
            self.updated_at = datetime.now().isoformat()
            return True
        return False
    
    def update_priority(self, new_priority: str):
        """Update task priority."""
        valid_priorities = ['low', 'medium', 'high']
        if new_priority in valid_priorities:
            self.priority = new_priority
            self.updated_at = datetime.now().isoformat()
            return True
        return False
    
    def __str__(self) -> str:
        """String representation of task."""
        return f"[{self.id}] {self.title} | Priority: {self.priority} | Status: {self.status}"


class TaskFlow:
    """Main TaskFlow application class for managing tasks."""
    
    def __init__(self, data_file: str = "taskflow_data.json"):
        self.data_file = data_file
        self.tasks: List[Task] = []
        self.next_id = 1
        self.load_tasks()
    
    def load_tasks(self):
        """Load tasks from JSON file."""
        if os.path.exists(self.data_file):
            try:
                with open(self.data_file, 'r') as f:
                    data = json.load(f)
                    self.tasks = [Task.from_dict(task_data) for task_data in data.get('tasks', [])]
                    self.next_id = data.get('next_id', 1)
            except (json.JSONDecodeError, KeyError):
                self.tasks = []
                self.next_id = 1
    
    def save_tasks(self):
        """Save tasks to JSON file."""
        data = {
            'tasks': [task.to_dict() for task in self.tasks],
            'next_id': self.next_id
        }
        with open(self.data_file, 'w') as f:
            json.dump(data, f, indent=2)
    
    def create_task(self, title: str, description: str = "", 
                   priority: str = "medium") -> Task:
        """Create a new task."""
        task = Task(self.next_id, title, description, priority)
        self.tasks.append(task)
        self.next_id += 1
        self.save_tasks()
        return task
    
    def get_task(self, task_id: int) -> Optional[Task]:
        """Get a task by ID."""
        for task in self.tasks:
            if task.id == task_id:
                return task
        return None
    
    def get_all_tasks(self) -> List[Task]:
        """Get all tasks."""
        return self.tasks
    
    def get_tasks_by_status(self, status: str) -> List[Task]:
        """Get tasks by status."""
        return [task for task in self.tasks if task.status == status]
    
    def get_tasks_by_priority(self, priority: str) -> List[Task]:
        """Get tasks by priority."""
        return [task for task in self.tasks if task.priority == priority]
    
    def update_task(self, task_id: int, title: Optional[str] = None,
                   description: Optional[str] = None,
                   priority: Optional[str] = None,
                   status: Optional[str] = None) -> bool:
        """Update a task."""
        task = self.get_task(task_id)
        if not task:
            return False
        
        if title is not None:
            task.title = title
        if description is not None:
            task.description = description
        if priority is not None:
            task.update_priority(priority)
        if status is not None:
            task.update_status(status)
        
        task.updated_at = datetime.now().isoformat()
        self.save_tasks()
        return True
    
    def delete_task(self, task_id: int) -> bool:
        """Delete a task."""
        task = self.get_task(task_id)
        if task:
            self.tasks.remove(task)
            self.save_tasks()
            return True
        return False
    
    def list_tasks(self, filter_status: Optional[str] = None,
                  filter_priority: Optional[str] = None) -> List[Task]:
        """List tasks with optional filters."""
        tasks = self.tasks
        
        if filter_status:
            tasks = [t for t in tasks if t.status == filter_status]
        
        if filter_priority:
            tasks = [t for t in tasks if t.priority == filter_priority]
        
        return tasks
    
    def get_statistics(self) -> Dict:
        """Get task statistics."""
        total = len(self.tasks)
        by_status = {}
        by_priority = {}
        
        for task in self.tasks:
            by_status[task.status] = by_status.get(task.status, 0) + 1
            by_priority[task.priority] = by_priority.get(task.priority, 0) + 1
        
        return {
            'total_tasks': total,
            'by_status': by_status,
            'by_priority': by_priority
        }


def main():
    """Main CLI interface for TaskFlow."""
    import sys
    
    tf = TaskFlow()
    
    if len(sys.argv) < 2:
        print("TaskFlow - Task Management System")
        print("\nUsage:")
        print("  python taskflow.py add <title> [description] [priority]")
        print("  python taskflow.py list [status] [priority]")
        print("  python taskflow.py view <task_id>")
        print("  python taskflow.py update <task_id> [--title <title>] [--description <desc>] [--priority <priority>] [--status <status>]")
        print("  python taskflow.py delete <task_id>")
        print("  python taskflow.py stats")
        print("\nStatus: pending, in_progress, completed, cancelled")
        print("Priority: low, medium, high")
        return
    
    command = sys.argv[1].lower()
    
    if command == "add":
        if len(sys.argv) < 3:
            print("Error: Title is required")
            return
        
        title = sys.argv[2]
        description = sys.argv[3] if len(sys.argv) > 3 else ""
        priority = sys.argv[4] if len(sys.argv) > 4 else "medium"
        
        task = tf.create_task(title, description, priority)
        print(f"Task created successfully!")
        print(task)
    
    elif command == "list":
        filter_status = sys.argv[2] if len(sys.argv) > 2 else None
        filter_priority = sys.argv[3] if len(sys.argv) > 3 else None
        
        tasks = tf.list_tasks(filter_status, filter_priority)
        
        if not tasks:
            print("No tasks found.")
        else:
            print(f"Total tasks: {len(tasks)}")
            print("-" * 60)
            for task in tasks:
                print(task)
                if task.description:
                    print(f"  Description: {task.description}")
                print()
    
    elif command == "view":
        if len(sys.argv) < 3:
            print("Error: Task ID is required")
            return
        
        try:
            task_id = int(sys.argv[2])
            task = tf.get_task(task_id)
            
            if task:
                print(f"Task ID: {task.id}")
                print(f"Title: {task.title}")
                print(f"Description: {task.description}")
                print(f"Priority: {task.priority}")
                print(f"Status: {task.status}")
                print(f"Created: {task.created_at}")
                print(f"Updated: {task.updated_at}")
            else:
                print(f"Task with ID {task_id} not found.")
        except ValueError:
            print("Error: Invalid task ID")
    
    elif command == "update":
        if len(sys.argv) < 3:
            print("Error: Task ID is required")
            return
        
        try:
            task_id = int(sys.argv[2])
            updates = {}
            
            i = 3
            while i < len(sys.argv):
                if sys.argv[i] == "--title" and i + 1 < len(sys.argv):
                    updates['title'] = sys.argv[i + 1]
                    i += 2
                elif sys.argv[i] == "--description" and i + 1 < len(sys.argv):
                    updates['description'] = sys.argv[i + 1]
                    i += 2
                elif sys.argv[i] == "--priority" and i + 1 < len(sys.argv):
                    updates['priority'] = sys.argv[i + 1]
                    i += 2
                elif sys.argv[i] == "--status" and i + 1 < len(sys.argv):
                    updates['status'] = sys.argv[i + 1]
                    i += 2
                else:
                    i += 1
            
            if tf.update_task(task_id, **updates):
                print(f"Task {task_id} updated successfully!")
                print(tf.get_task(task_id))
            else:
                print(f"Task with ID {task_id} not found.")
        except ValueError:
            print("Error: Invalid task ID")
    
    elif command == "delete":
        if len(sys.argv) < 3:
            print("Error: Task ID is required")
            return
        
        try:
            task_id = int(sys.argv[2])
            if tf.delete_task(task_id):
                print(f"Task {task_id} deleted successfully!")
            else:
                print(f"Task with ID {task_id} not found.")
        except ValueError:
            print("Error: Invalid task ID")
    
    elif command == "stats":
        stats = tf.get_statistics()
        print("TaskFlow Statistics")
        print("=" * 40)
        print(f"Total Tasks: {stats['total_tasks']}")
        print("\nBy Status:")
        for status, count in stats['by_status'].items():
            print(f"  {status}: {count}")
        print("\nBy Priority:")
        for priority, count in stats['by_priority'].items():
            print(f"  {priority}: {count}")
    
    else:
        print(f"Unknown command: {command}")


if __name__ == "__main__":
    main()
