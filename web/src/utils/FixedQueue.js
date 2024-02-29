// 定长队列
export default class FixedQueue {
  constructor(length) {
    this.queue = [];
    this.maxLength = length;
  }

  enqueue(items) {
    if (this.queue.length + items.length > this.maxLength) {
      const count = this.queue.length + items.length - this.maxLength;
      this.queue.splice(0, count); // 移除超出最大长度的元素
    }
    this.queue.push(...items);
  }

  dequeue() {
    return this.queue.shift();
  }

  peek() {
    return this.queue[0];
  }

  size() {
    return this.queue.length;
  }

  isEmpty() {
    return this.queue.length === 0;
  }

  getArray(){
    return this.queue;
  }
}
