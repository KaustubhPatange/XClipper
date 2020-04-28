using ClipboardManager.models;
using SQLite;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using static Components.MainHelper;
using static WK.Libraries.SharpClipboardNS.SharpClipboard;
using static Components.DefaultSettings;
using System.Diagnostics;
using static Components.Core;
using static Components.Constants;

namespace Components.viewModels
{
    public sealed class AppSingleton
    {
        private IClipBinder Binder;
        public SQLiteConnection dataDB;
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
        { }
        
        public void Init()
        {
            SQLiteConnectionString options;
            if (IsSecureDB)
                options = new SQLiteConnectionString(DatabasePath, true, CustomPassword);
            else 
                options = new SQLiteConnectionString(DatabasePath, true);
            dataDB = new SQLiteConnection(options);
            dataDB.CreateTable<TableCopy>();
        }
        public void SetBinder(IClipBinder Binder)
        {
            if (dataDB == null)
                Init();
            this.Binder = Binder;
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
            models.ForEach((model) => { dataDB.Delete(model); });
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
            // We will encrypt data here coz... why not...
            dataDB.Execute("update TableCopy set Text = ?, LongText = ?, RawText = ? where Id = ?", model.Text, model.LongText, model.RawText, model.Id);
            Binder.OnPopupTextEdited(ClipData);
        }

        public void UpdateLastUsedTime(TableCopy model)
        {
            dataDB.Execute("update TableCopy set LastUsedDateTime = ? where Id = ?", model.LastUsedDateTime, model.Id);
        }

        public List<TableCopy> FilterTextLengthDesc()
        {
            var data = dataDB.Query<TableCopy>("select * from TableCopy");
            return data.Where(x => x.ContentType == ContentType.Text).OrderByDescending(x => x.RawText.Length).Take(TruncateList).ToList();
        }

        public List<TableCopy> FilterTextLengthAsc()
        {
            var data = dataDB.Query<TableCopy>("select * from TableCopy");
            return data.Where(x => x.ContentType == ContentType.Text).OrderBy(x => x.RawText.Length).Take(TruncateList).ToList();
        }
        public List<TableCopy> FilterOldest() => dataDB.Table<TableCopy>().Take(TruncateList).ToList();
        public List<TableCopy> FilterNewest() => dataDB.Table<TableCopy>().Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterData(string text) => dataDB.Table<TableCopy>().Where(s => s.Text.ToLower().Contains(text.ToLower())).Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterContentType(ContentType type) => dataDB.Table<TableCopy>().Where(s => s.ContentType == type).Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterPinned() => dataDB.Table<TableCopy>().Where(s => s.IsPinned).Reverse().Take(TruncateList).ToList();
        public List<TableCopy> FilterUnpinned() => dataDB.Table<TableCopy>().Where(s => !s.IsPinned).Reverse().Take(TruncateList).ToList();

        #endregion

        public List<TableCopy> ClipData
        {
            get
            {
                var pinnedItems = dataDB.Query<TableCopy>("select * from TableCopy where IsPinned = 1");
                pinnedItems.Reverse();

                var normalItems = dataDB.Query<TableCopy>("select * from TableCopy where IsPinned = 0")
                    .OrderByDescending(x => ParseDateTimeText(x.LastUsedDateTime));

                return pinnedItems.Concat(normalItems).Take(TruncateList).ToList();
            }
        }

        #region Insert Content

        public void InsertContent(TableCopy model)
        {
            // This will check if same clip text is saved again
            var list = dataDB.Query<TableCopy>("select * from TableCopy").OrderByDescending(s => ParseDateTimeText(s.LastUsedDateTime)).ToList();
            foreach (var c in list)
            {
                if (c.ContentType == model.ContentType)
                {
                    switch (model.ContentType)
                    {
                        case ContentType.Text:
                            if (model.Text == c.Text) return;
                            break;
                        case ContentType.Image:
                            if (model.ImagePath == c.ImagePath) return;
                            break;
                        case ContentType.Files:
                            if (model.LongText == c.LongText) return;
                            break;
                    }
                }
            }

            // Implementation of setting TotalClipLength 
            if (list.Count >= TotalClipLength)
            {
                dataDB.Delete(list[list.Count - 1]);
            }

            dataDB.Insert(model);
        }


        #endregion

    }

}
