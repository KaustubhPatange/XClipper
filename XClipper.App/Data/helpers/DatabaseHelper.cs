using ClipboardManager.models;
using SQLite;
using SQLiteNetExtensions.Extensions;
using System;
using System.Collections.Generic;
using System.Windows.Documents;
using static Components.Constants;
using static Components.DefaultSettings;

namespace Components
{
    public class DatabaseHelper : IDatabase<TableCopy>
    {
        private SQLiteConnection dataDB;
        public void Initialize()
        {
            SQLiteConnectionString options;
            if (IsSecureDB)
                options = new SQLiteConnectionString(DatabasePath, true, CustomPassword);
            else
                options = new SQLiteConnectionString(DatabasePath, true);
            dataDB = new SQLiteConnection(options);
            dataDB.CreateTable<TableCopy>();
        }
        public void Delete(TableCopy model)
        {
            dataDB.Delete(model);
        }

        public void Delete(List<TableCopy> models)
        {
            dataDB.DeleteAll(models);
        }

        public void Insert(TableCopy model)
        {
            dataDB.Insert(model);
        }

        public void InsertAll(List<TableCopy> models)
        {
            dataDB.InsertAll(models);
        }

        public List<TableCopy> GetAllData() => dataDB.Table<TableCopy>().ToList();

        public void Update(TableCopy model)
        {
            dataDB.Update(model);
        }

        public void Query(string query, params object[] args)
        {
            dataDB.Execute(query, args);
        }

        public void ClearAll<TableCopy>()
        {
            dataDB.DeleteAll<TableCopy>();
        }

        public void CloseConnection()
        {
            dataDB.Close();
        }
      
    }
}
