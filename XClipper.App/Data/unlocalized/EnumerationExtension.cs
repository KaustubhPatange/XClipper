using System;
using System.ComponentModel;
using System.Linq;
using System.Windows.Markup;

namespace Components
{
    public class EnumerationExtension : MarkupExtension
    {
        private Type _enumType;
        public EnumerationExtension(Type enumType)
        {
            if (enumType == null)
                throw new ArgumentNullException("enumType");

            EnumType = enumType;
        }

        public Type EnumType
        {
            get { return _enumType; }
            private set
            {
                if (_enumType == value)
                    return;

                var enumType = Nullable.GetUnderlyingType(value) ?? value;

                if (enumType.IsEnum == false)
                    throw new ArgumentException("Type must be an Enum.");

                _enumType = value;
            }
        }
        private string GetDescription(object enumValue)
        {
            var descriptionAttribute = EnumType
              .GetField(enumValue.ToString())
              .GetCustomAttributes(typeof(DescriptionAttribute), false)
              .FirstOrDefault() as DescriptionAttribute;


            return descriptionAttribute != null
              ? descriptionAttribute.Description
              : enumValue.ToString();
        }
        public class EnumerationMember
        {
            public string Description { get; set; }
            public object Value { get; set; }
        }
        public override object ProvideValue(IServiceProvider serviceProvider)
        {
            var enumArray = Enum.GetValues(EnumType);

            var enumValues = new string[enumArray.Length];
            for (int i=0; i < enumValues.Length; i++)
            {
                enumValues[i] = EnumHelper.GetEnumDescription(enumArray.GetValue(i) as Enum);
            }
            return enumValues;
        }

    }
}
