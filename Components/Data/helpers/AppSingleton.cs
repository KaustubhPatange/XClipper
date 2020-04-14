using ClipboardManager.models;
using SQLite;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Components.viewModels
{
    public sealed class AppSingleton
    {
        private ClipBinder Binder;
        private static string BaseDirectory = AppDomain.CurrentDomain.BaseDirectory;
        private string DatabasePath;

        private SQLiteConnection dataDB;
        private static AppSingleton Instance = null;
        public static AppSingleton GetInstance
        {
            get
            {
                if (Instance == null)
                    Instance = new AppSingleton();
                return Instance;
            }
        }

        private AppSingleton()
        {}

        public void SetBinder(ClipBinder Binder)
        {
            this.Binder = Binder;
            DatabasePath = Path.Combine(BaseDirectory, "data.db");
            dataDB = new SQLiteConnection(DatabasePath);
        }

        public void SetFilterText(string Text) => Binder.OnFilterTextEdit(Text);

        #region Data Filtering

        public void DeleteData(TableCopy model)
        {
            dataDB.Delete(model);
            Binder.OnModelDeleted(ClipData);
        }
        public void DeleteData(List<TableCopy> models)
        {
            models.ForEach((model) => { dataDB.Delete(model);});
            Binder.OnModelDeleted(ClipData);
        }
        public void TogglePin(TableCopy model)
        {
            model.IsPinned = !model.IsPinned;
            dataDB.Execute("update TableCopy set IsPinned = ? where Id = ?", model.IsPinned, model.Id);
            Binder.OnModelDeleted(ClipData);
        }
        public void UpdateData(TableCopy model)
        {
            dataDB.Execute("update TableCopy set Text = ?, LongText = ?, RawText = ? where Id = ?", model.Text, model.LongText, model.RawText, model.Id);
            Binder.OnPopupTextEdited(ClipData);
        }

        public List<TableCopy> FilterTextLengthDesc()
        {
            var data = ClipData;
            return data.OrderByDescending(x => x.RawText.Length).Where(x => x.ContentType == ContentType.Text).ToList();
        }

        public List<TableCopy> FilterTextLengthAsc()
        {
            var data = ClipData;
            return data.OrderBy(x=> x.RawText.Length).Where(x => x.ContentType == ContentType.Text).ToList();
        }
        public List<TableCopy> FilterOldest() => dataDB.Table<TableCopy>().ToList();
        public List<TableCopy> FilterData(string text) => dataDB.Table<TableCopy>().Where(s => s.Text.ToLower().Contains(text.ToLower())).Reverse().ToList();
        public List<TableCopy> FilterContentType(ContentType type) => dataDB.Table<TableCopy>().Where(s => s.ContentType == type).Reverse().ToList();
        public List<TableCopy> FilterPinned() => dataDB.Table<TableCopy>().Where(s => s.IsPinned).Reverse().ToList();
        public List<TableCopy> FilterUnpinned() => dataDB.Table<TableCopy>().Where(s => !s.IsPinned).Reverse().ToList();

        #endregion
        public List<TableCopy> ClipData
        {
            get
            {
                var pinnedItems = dataDB.Query<TableCopy>("select * from TableCopy where IsPinned = 1");
                pinnedItems.Reverse();
                var normalItems = dataDB.Query<TableCopy>("select * from TableCopy where IsPinned = 0");
                normalItems.Reverse();
                return pinnedItems.Concat(normalItems).ToList();
             //   return dataDB.Table<TableCopy>().Reverse().ToList();
            }
        }
    
    }
   
}
