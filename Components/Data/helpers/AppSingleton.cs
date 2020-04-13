using ClipboardManager.models;
using SQLite;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Components.viewModels
{
    public sealed class AppSingleton
    {
        private ClipBinder binder;
        private static string baseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        private string databasePath;

        private SQLiteConnection dataDB;
        private static AppSingleton instance = null;
        public static AppSingleton GetInstance
        {
            get
            {
                if (instance == null)
                    instance = new AppSingleton();
                return instance;
            }
        }

        private AppSingleton()
        {}

        public void setBinder(ClipBinder binder)
        {
            this.binder = binder;
            databasePath = Path.Combine(baseDirectory, "data.db");
            dataDB = new SQLiteConnection(databasePath);
        }

        public void DeleteData(TableCopy model)
        {
            dataDB.Delete(model);
            binder.OnModelDeleted(ClipData);
        }

        public void DeleteData(List<TableCopy> models)
        {
            models.ForEach((model) => { dataDB.Delete(model);});
            binder.OnModelDeleted(ClipData);
        }
        public void UpdateData(TableCopy model)
        {
            dataDB.Execute("update TableCopy set Text = ?, LongText = ?, RawText = ? where Id = ?", model.Text, model.LongText, model.RawText, model.Id);
            binder.OnPopupTextEdited(ClipData);
        }

        public List<TableCopy> FilterData(string text)
        {
            return dataDB.Table<TableCopy>().Where(s => s.Text.Contains(text)).Reverse().ToList();
        }

        public List<TableCopy> ClipData
        {
            get
            {
                return dataDB.Table<TableCopy>().Reverse().ToList();
            }
        }
    
    }
   
}
