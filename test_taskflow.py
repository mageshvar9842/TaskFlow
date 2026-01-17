#!/usr/bin/env python3
"""
Unit tests for TaskFlow
"""

import unittest
import os
import json
from taskflow import Task, TaskFlow


class TestTask(unittest.TestCase):
    """Test cases for Task class."""
    
    def test_task_creation(self):
        """Test creating a new task."""
        task = Task(1, "Test Task", "Test Description", "high", "pending")
        self.assertEqual(task.id, 1)
        self.assertEqual(task.title, "Test Task")
        self.assertEqual(task.description, "Test Description")
        self.assertEqual(task.priority, "high")
        self.assertEqual(task.status, "pending")
    
    def test_task_to_dict(self):
        """Test converting task to dictionary."""
        task = Task(1, "Test Task", "Test Description")
        task_dict = task.to_dict()
        self.assertEqual(task_dict['id'], 1)
        self.assertEqual(task_dict['title'], "Test Task")
        self.assertEqual(task_dict['description'], "Test Description")
    
    def test_task_from_dict(self):
        """Test creating task from dictionary."""
        task_data = {
            'id': 1,
            'title': "Test Task",
            'description': "Test Description",
            'priority': "high",
            'status': "pending"
        }
        task = Task.from_dict(task_data)
        self.assertEqual(task.id, 1)
        self.assertEqual(task.title, "Test Task")
        self.assertEqual(task.priority, "high")
    
    def test_update_status(self):
        """Test updating task status."""
        task = Task(1, "Test Task")
        self.assertTrue(task.update_status("completed"))
        self.assertEqual(task.status, "completed")
        self.assertFalse(task.update_status("invalid_status"))
    
    def test_update_priority(self):
        """Test updating task priority."""
        task = Task(1, "Test Task")
        self.assertTrue(task.update_priority("high"))
        self.assertEqual(task.priority, "high")
        self.assertFalse(task.update_priority("invalid_priority"))


class TestTaskFlow(unittest.TestCase):
    """Test cases for TaskFlow class."""
    
    def setUp(self):
        """Set up test fixtures."""
        self.test_file = "test_taskflow_data.json"
        if os.path.exists(self.test_file):
            os.remove(self.test_file)
        self.tf = TaskFlow(self.test_file)
    
    def tearDown(self):
        """Clean up test fixtures."""
        if os.path.exists(self.test_file):
            os.remove(self.test_file)
    
    def test_create_task(self):
        """Test creating a task."""
        task = self.tf.create_task("Test Task", "Description", "high")
        self.assertEqual(task.title, "Test Task")
        self.assertEqual(task.description, "Description")
        self.assertEqual(task.priority, "high")
        self.assertEqual(len(self.tf.tasks), 1)
    
    def test_get_task(self):
        """Test getting a task by ID."""
        task = self.tf.create_task("Test Task")
        retrieved_task = self.tf.get_task(task.id)
        self.assertIsNotNone(retrieved_task)
        self.assertEqual(retrieved_task.id, task.id)
        self.assertEqual(retrieved_task.title, "Test Task")
    
    def test_get_task_not_found(self):
        """Test getting a non-existent task."""
        task = self.tf.get_task(999)
        self.assertIsNone(task)
    
    def test_get_all_tasks(self):
        """Test getting all tasks."""
        self.tf.create_task("Task 1")
        self.tf.create_task("Task 2")
        self.tf.create_task("Task 3")
        tasks = self.tf.get_all_tasks()
        self.assertEqual(len(tasks), 3)
    
    def test_get_tasks_by_status(self):
        """Test getting tasks by status."""
        task1 = self.tf.create_task("Task 1")
        task2 = self.tf.create_task("Task 2")
        task2.update_status("completed")
        self.tf.save_tasks()
        
        pending_tasks = self.tf.get_tasks_by_status("pending")
        completed_tasks = self.tf.get_tasks_by_status("completed")
        
        self.assertEqual(len(pending_tasks), 1)
        self.assertEqual(len(completed_tasks), 1)
    
    def test_get_tasks_by_priority(self):
        """Test getting tasks by priority."""
        self.tf.create_task("Task 1", priority="low")
        self.tf.create_task("Task 2", priority="high")
        self.tf.create_task("Task 3", priority="high")
        
        high_priority = self.tf.get_tasks_by_priority("high")
        low_priority = self.tf.get_tasks_by_priority("low")
        
        self.assertEqual(len(high_priority), 2)
        self.assertEqual(len(low_priority), 1)
    
    def test_update_task(self):
        """Test updating a task."""
        task = self.tf.create_task("Original Title")
        success = self.tf.update_task(
            task.id,
            title="Updated Title",
            description="Updated Description",
            priority="high",
            status="in_progress"
        )
        self.assertTrue(success)
        
        updated_task = self.tf.get_task(task.id)
        self.assertEqual(updated_task.title, "Updated Title")
        self.assertEqual(updated_task.description, "Updated Description")
        self.assertEqual(updated_task.priority, "high")
        self.assertEqual(updated_task.status, "in_progress")
    
    def test_update_task_not_found(self):
        """Test updating a non-existent task."""
        success = self.tf.update_task(999, title="New Title")
        self.assertFalse(success)
    
    def test_update_task_invalid_values(self):
        """Test updating a task with invalid priority or status."""
        task = self.tf.create_task("Test Task")
        
        # Test invalid priority
        success = self.tf.update_task(task.id, priority="invalid")
        self.assertFalse(success)
        
        # Test invalid status
        success = self.tf.update_task(task.id, status="invalid")
        self.assertFalse(success)
        
        # Verify task unchanged
        updated_task = self.tf.get_task(task.id)
        self.assertEqual(updated_task.priority, "medium")  # default
        self.assertEqual(updated_task.status, "pending")  # default
    
    def test_delete_task(self):
        """Test deleting a task."""
        task = self.tf.create_task("Task to Delete")
        task_id = task.id
        
        success = self.tf.delete_task(task_id)
        self.assertTrue(success)
        self.assertEqual(len(self.tf.tasks), 0)
        self.assertIsNone(self.tf.get_task(task_id))
    
    def test_delete_task_not_found(self):
        """Test deleting a non-existent task."""
        success = self.tf.delete_task(999)
        self.assertFalse(success)
    
    def test_list_tasks_with_filters(self):
        """Test listing tasks with filters."""
        self.tf.create_task("Task 1", priority="high")
        self.tf.create_task("Task 2", priority="low")
        self.tf.create_task("Task 3", priority="high")
        
        # Update statuses
        self.tf.update_task(2, status="completed")
        self.tf.update_task(3, status="completed")
        
        high_priority = self.tf.list_tasks(filter_priority="high")
        completed = self.tf.list_tasks(filter_status="completed")
        high_completed = self.tf.list_tasks(filter_status="completed", filter_priority="high")
        
        self.assertEqual(len(high_priority), 2)
        self.assertEqual(len(completed), 2)
        self.assertEqual(len(high_completed), 1)
    
    def test_get_statistics(self):
        """Test getting task statistics."""
        self.tf.create_task("Task 1", priority="high")
        self.tf.create_task("Task 2", priority="low")
        self.tf.create_task("Task 3", priority="high")
        self.tf.update_task(2, status="completed")
        
        stats = self.tf.get_statistics()
        
        self.assertEqual(stats['total_tasks'], 3)
        self.assertEqual(stats['by_priority']['high'], 2)
        self.assertEqual(stats['by_priority']['low'], 1)
        self.assertEqual(stats['by_status']['pending'], 2)
        self.assertEqual(stats['by_status']['completed'], 1)
    
    def test_persistence(self):
        """Test task persistence to file."""
        self.tf.create_task("Task 1", "Description 1", "high")
        self.tf.create_task("Task 2", "Description 2", "low")
        
        # Create new instance to load from file
        tf2 = TaskFlow(self.test_file)
        
        self.assertEqual(len(tf2.tasks), 2)
        self.assertEqual(tf2.tasks[0].title, "Task 1")
        self.assertEqual(tf2.tasks[1].title, "Task 2")
        self.assertEqual(tf2.next_id, 3)


if __name__ == '__main__':
    unittest.main()
