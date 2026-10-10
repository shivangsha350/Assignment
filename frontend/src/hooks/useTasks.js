import { useState, useEffect } from 'react';
import { fetchTasks } from '../api';

export function useTasks(query, status, page, pageSize) {
  const [tasks, setTasks] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [debouncedQuery, setDebouncedQuery] = useState(query);

  // Debounce search query input by 300ms
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedQuery(query);
    }, 300);

    return () => {
      clearTimeout(handler);
    };
  }, [query]);

  useEffect(() => {
    const controller = new AbortController();

    setLoading(true);
    setError(null);

    fetchTasks({
      query: debouncedQuery,
      status,
      page,
      pageSize,
      signal: controller.signal,
    })
      .then((data) => {
        setTasks(data.items || []);
        setTotal(data.total || 0);
        setLoading(false);
      })
      .catch((err) => {
        if (err.name === 'AbortError') {
          // Superseded request aborted; do not alter state
          return;
        }
        setError(err.message);
        setLoading(false);
      });

    return () => {
      controller.abort();
    };
  }, [debouncedQuery, status, page, pageSize]);

  return { tasks, total, loading, error };
}
