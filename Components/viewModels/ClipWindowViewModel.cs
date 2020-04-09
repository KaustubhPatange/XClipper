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
    public sealed class ClipWindowViewModel
    {
        private ClipBinder binder;
        private static string baseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        private string databasePath;

        private SQLiteConnection dataDB;
        private static ClipWindowViewModel instance = null;
        public static ClipWindowViewModel GetInstance
        {
            get
            {
                if (instance == null)
                    instance = new ClipWindowViewModel();
                return instance;
            }
        }

        private ClipWindowViewModel()
        {}

        public void setBinder(ClipBinder binder)
        {
            this.binder = binder;
            databasePath = Path.Combine(baseDirectory, "data.db");
            dataDB = new SQLiteConnection(databasePath);

        }
        public void UpdateData(TableCopy model)
        {
            dataDB.Execute("update TableCopy set Text = ? where Id = ?", model.Text, model.Id);
            binder.OnUpdate(ClipData);
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
