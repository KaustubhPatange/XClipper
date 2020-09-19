using System.Collections.Generic;

namespace Components
{
    public interface IDatabase<T>
    {
        void Initialize();
        void Insert(T model);
        void Update(T model);
        void Delete(T model);
        void Delete(List<T> models);
        void InsertAll(List<T> models);
        List<T> GetAllData();
        void ClearAll<T>();
        void CloseConnection();
        void Query(string query, params object[] args);
    }
}
