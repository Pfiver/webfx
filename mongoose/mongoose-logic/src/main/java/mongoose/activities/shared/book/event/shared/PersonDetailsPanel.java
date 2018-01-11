package mongoose.activities.shared.book.event.shared;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import mongoose.activities.shared.generic.MongooseButtonFactoryMixin;
import mongoose.activities.shared.generic.MongooseSectionFactoryMixin;
import mongoose.activities.shared.logic.ui.validation.MongooseValidationSupport;
import mongoose.auth.MongooseUser;
import mongoose.domainmodel.format.DateFormatter;
import mongoose.domainmodel.functions.AbcNames;
import mongoose.entities.Country;
import mongoose.entities.Event;
import mongoose.entities.Organization;
import mongoose.entities.Person;
import mongoose.entities.markers.HasPersonDetails;
import naga.framework.activity.view.ViewActivityContextMixin;
import naga.framework.orm.domainmodel.DataSourceModel;
import naga.framework.orm.entity.Entity;
import naga.framework.orm.entity.EntityStore;
import naga.framework.ui.auth.UiUser;
import naga.framework.ui.controls.EntityButtonSelector;
import naga.framework.ui.controls.GridPaneBuilder;
import naga.framework.ui.controls.material.MaterialLabel;
import naga.framework.ui.i18n.I18n;
import naga.framework.ui.layouts.LayoutUtil;
import naga.fx.properties.Properties;
import naga.fx.spi.Toolkit;
import naga.fxdata.control.DataGrid;
import naga.fxdata.displaydata.DisplayColumn;
import naga.fxdata.displaydata.DisplayResultSetBuilder;
import naga.fxdata.displaydata.DisplayStyle;
import naga.fxdata.displaydata.SelectionMode;
import naga.platform.services.auth.spi.User;
import naga.type.PrimType;
import naga.util.Booleans;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public class PersonDetailsPanel implements MongooseButtonFactoryMixin, MongooseSectionFactoryMixin {
    private final Event event;
    private final I18n i18n;
    private final TextField firstNameTextField, lastNameTextField, carer1NameTextField, carer2NameTextField, emailTextField, phoneTextField, streetTextField, postCodeTextField, cityNameTextField;
    private final RadioButton maleRadioButton, femaleRadioButton, childRadioButton, adultRadioButton;
    private final DatePicker birthDatePicker;
    private final EntityButtonSelector personSelector, countrySelector, organizationSelector;
    private final MaterialLabel personButton, countryButton, organizationButton;
    private final BorderPane sectionPanel;
    private HasPersonDetails model;
    private boolean editable = true;
    private final MongooseValidationSupport validationSupport = new MongooseValidationSupport();

    private static final int CHILD_MAX_AGE = 17;

    public PersonDetailsPanel(Event event, ViewActivityContextMixin viewActivityContextMixin, Pane parent) {
        this(event, viewActivityContextMixin, parent, null);
    }

    public PersonDetailsPanel(Event event, ViewActivityContextMixin viewActivityContextMixin, Pane parent, UiUser uiUser) {
        this.event = event;
        i18n = viewActivityContextMixin.getI18n();
        sectionPanel = createSectionPanel("YourPersonalDetails");

        firstNameTextField = newMaterialTextFieldWithPrompt("FirstName");
        lastNameTextField = newMaterialTextFieldWithPrompt("LastName");
        maleRadioButton = newRadioButton("Male");
        femaleRadioButton = newRadioButton("Female");
        ToggleGroup genderGroup = new ToggleGroup();
        maleRadioButton.setToggleGroup(genderGroup);
        femaleRadioButton.setToggleGroup(genderGroup);
        adultRadioButton = newRadioButton("Adult");
        childRadioButton = newRadioButton("Child");
        ToggleGroup ageGroup = new ToggleGroup();
        childRadioButton.setToggleGroup(ageGroup);
        adultRadioButton.setToggleGroup(ageGroup);
        birthDatePicker = LayoutUtil.setMaxWidthToInfinite(new DatePicker());
        birthDatePicker.setConverter(DateFormatter.LOCAL_DATE_STRING_CONVERTER);
        carer1NameTextField = newMaterialTextFieldWithPrompt("Carer1");
        carer2NameTextField = newMaterialTextFieldWithPrompt("Carer2");
        emailTextField = newMaterialTextFieldWithPrompt("Email");
        phoneTextField = newMaterialTextFieldWithPrompt("Phone");
        streetTextField = newMaterialTextFieldWithPrompt("Street");
        postCodeTextField = newMaterialTextFieldWithPrompt("Postcode");
        cityNameTextField = newMaterialTextFieldWithPrompt("City");
        DataSourceModel dataSourceModel = event.getStore().getDataSourceModel();
        countrySelector = createEntityButtonSelector("{class: 'Country', orderBy: 'name'}", viewActivityContextMixin, parent, dataSourceModel);
        countryButton = newMaterialEntityButton(countrySelector, "Country");
        organizationSelector = createEntityButtonSelector("{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC`', orderBy: 'country.name,name'}", viewActivityContextMixin, parent, dataSourceModel);
        organizationButton = newMaterialEntityButton(organizationSelector, "Centre");
        if (uiUser == null) {
            personSelector = null;
            personButton = null;
        } else {
            personSelector = createEntityButtonSelector(null, viewActivityContextMixin, parent, dataSourceModel);
            personButton = newMaterialEntityButton(personSelector, "PersonToBook");
            Properties.runOnPropertiesChange(p -> syncUiFromModel((Person) p.getValue()), personSelector.entityProperty());
            Properties.runNowAndOnPropertiesChange(userProperty -> {
                User user = (User) userProperty.getValue();
                boolean loggedIn = user instanceof MongooseUser;
                if (loggedIn) {
                    Object userAccountPrimaryKey = ((MongooseUser) user).getUserAccountPrimaryKey();
                    personSelector.setJsonOrClass("{class: 'Person', alias: 'p', fields: 'genderIcon,firstName,lastName,birthdate,email,phone,street,postCode,cityName,organization,country', columns: `[{expression: 'genderIcon,firstName,lastName'}]`, where: '!removed and frontendAccount=" + userAccountPrimaryKey + "', orderBy: 'id'}");
                    personSelector.autoSelectFirstEntity();
                } else
                    personSelector.setJsonOrClass(null);
                personButton.setVisible(loggedIn);
            }, uiUser.userProperty());
        }
        initValidation();
    }

    private void initValidation() {
        validationSupport.addRequiredInputs(firstNameTextField, lastNameTextField, emailTextField, phoneTextField, carer1NameTextField, carer2NameTextField);
        validationSupport.addNotEmptyControlValidation(countrySelector.entityProperty(), countrySelector.getEntityButton());
    }

    public boolean isValid() {
        return validationSupport.isValid();
    }

    private static EntityButtonSelector createEntityButtonSelector(Object jsonOrClass, ViewActivityContextMixin viewActivityContextMixin, Pane parent, DataSourceModel dataSourceModel) {
        return new EntityButtonSelector(jsonOrClass, viewActivityContextMixin, parent, dataSourceModel) {
            @Override
            protected void setSearchParameters(String search, EntityStore store) {
                super.setSearchParameters(search, store);
                store.setParameterValue("abcSearchLike", AbcNames.evaluate(search, true));
            }
        };
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }

    public void setLoadingStore(EntityStore store) {
        countrySelector.setLoadingStore(store);
        organizationSelector.setLoadingStore(store);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        updateUiEditable();
    }

    private void updateUiEditable() {
        boolean profileEditable = editable && personSelector.getEntity() == null;
        boolean profileDisable = !profileEditable;
        firstNameTextField.setEditable(profileEditable);
        lastNameTextField.setEditable(profileEditable);
        maleRadioButton.setDisable(profileDisable);
        femaleRadioButton.setDisable(profileDisable);
        adultRadioButton.setDisable(profileDisable);
        childRadioButton.setDisable(profileDisable);
        birthDatePicker.setEditable(profileEditable);
        carer1NameTextField.setEditable(editable);
        carer2NameTextField.setEditable(editable);
        emailTextField.setEditable(editable);
        phoneTextField.setEditable(editable);
        streetTextField.setEditable(editable);
        postCodeTextField.setEditable(editable);
        cityNameTextField.setEditable(editable);
        countrySelector.setEditable(editable);
        organizationSelector.setEditable(editable);
    }

    public BorderPane getSectionPanel() {
        return sectionPanel;
    }

    private void updatePanelBody() {
        sectionPanel.setCenter(createPanelBody());
    }

    private Node createPanelBody() {
        return editable ? createPersonVBox() /*createPersonGridPane()*/ : createPersonDataGrid();
    }

    private GridPane createPersonGridPane() {
        GridPaneBuilder gridPaneBuilder = new GridPaneBuilder(i18n)
                .addLabelNodeRow("PersonToBook:", personButton)
                .addLabelTextInputRow("FirstName:", firstNameTextField)
                .addLabelTextInputRow("LastName:", lastNameTextField)
                .addLabelNodeRow("Gender:", new HBox(20, maleRadioButton, femaleRadioButton))
                .addLabelNodeRow("Age:", new HBox(20, adultRadioButton, childRadioButton));
        if (childRadioButton.isSelected())
            gridPaneBuilder
                    .addLabelNodeRow("BirthDate:", birthDatePicker)
                    .addLabelTextInputRow("Carer1:", carer1NameTextField)
                    .addLabelTextInputRow("Carer2:", carer2NameTextField);
        GridPane gridPane = gridPaneBuilder
                .addLabelTextInputRow("Email:", emailTextField)
                .addLabelTextInputRow("Phone:", phoneTextField)
                .addLabelTextInputRow("Street:", streetTextField)
                .addLabelTextInputRow("Postcode:", postCodeTextField)
                .addLabelTextInputRow("City:", cityNameTextField)
                .addLabelNodeRow("Country:", countryButton)
                .addLabelNodeRow("Centre:", organizationButton)
                .build();
        gridPane.setPadding(new Insets(10));
        return gridPane;
    }

    private VBox createPersonVBox() {
        VBox vBox = new VBox(8,
                LayoutUtil.setUnmanagedWhenInvisible(personButton),
                firstNameTextField,
                lastNameTextField,
                newMaterialRegion(new HBox(20, maleRadioButton, femaleRadioButton), "Gender"),
                newMaterialRegion(new HBox(20, adultRadioButton, childRadioButton), "Age")
        );
        if (childRadioButton.isSelected())
            vBox.getChildren().addAll(
                    newMaterialRegion(birthDatePicker, "BirthDate"),
                    carer1NameTextField,
                    carer2NameTextField
            );
        vBox.getChildren().addAll(
                emailTextField,
                phoneTextField,
                streetTextField,
                postCodeTextField,
                cityNameTextField,
                countryButton,
                organizationButton
        );
        vBox.setPadding(new Insets(10));
        return vBox;
    }

    private MaterialLabel newMaterialEntityButton(EntityButtonSelector entityButtonSelector, Object i18nKey) {
        return new MaterialLabel(LayoutUtil.setMaxWidthToInfinite(entityButtonSelector.getEntityButton()), entityButtonSelector.entityProperty());
    }

    private MaterialLabel newMaterialRegion(Region region, Object i18nKey) {
        return new MaterialLabel(region);
    }

    private Node createPersonDataGrid() {
        DisplayColumn keyColumn = DisplayColumn.create(null, PrimType.STRING, DisplayStyle.RIGHT_STYLE);
        DisplayColumn valueColumn = DisplayColumn.create(null, PrimType.STRING);
        DisplayResultSetBuilder rsb = DisplayResultSetBuilder.create(6, new DisplayColumn[]{keyColumn, valueColumn, keyColumn, valueColumn});
        Organization organization = model.getOrganization();
        rsb.setValue(0, 0, i18n.instantTranslate("FirstName:"));
        rsb.setValue(0, 1, model.getFirstName());
        rsb.setValue(1, 0, i18n.instantTranslate("LastName:"));
        rsb.setValue(1, 1, model.getLastName());
        rsb.setValue(2, 0, i18n.instantTranslate("Gender:"));
        rsb.setValue(2, 1, i18n.instantTranslate(Booleans.isTrue(model.isMale()) ? "Male" : "Female"));
        rsb.setValue(3, 0, i18n.instantTranslate("Age:"));
        rsb.setValue(3, 1, i18n.instantTranslate(model.getAge() == null ? "Adult" : model.getAge()));
        rsb.setValue(4, 0, i18n.instantTranslate("Email:"));
        rsb.setValue(4, 1, model.getEmail());
        rsb.setValue(5, 0, i18n.instantTranslate("Phone:"));
        rsb.setValue(5, 1, model.getPhone());
        rsb.setValue(0, 2, i18n.instantTranslate("Centre:"));
        rsb.setValue(0, 3, organization == null ? i18n.instantTranslate("NoCentre") : organization.getName());
        rsb.setValue(1, 2, i18n.instantTranslate("Street:"));
        rsb.setValue(1, 3, model.getStreet());
        rsb.setValue(2, 2, i18n.instantTranslate("Postcode:"));
        rsb.setValue(2, 3, model.getPostCode());
        rsb.setValue(3, 2, i18n.instantTranslate("City:"));
        rsb.setValue(3, 3, model.getCityName());
        rsb.setValue(4, 2, i18n.instantTranslate("State:"));
        //rsb.setValue(5, 1, model.getPostCode());
        rsb.setValue(5, 2, i18n.instantTranslate("Country:"));
        rsb.setValue(5, 3, model.getCountryName());
        DataGrid dataGrid = new DataGrid(rsb.build()); // LayoutUtil.setMinMaxHeightToPref(new DataGrid(rsb.build()));
        dataGrid.setHeaderVisible(false);
        dataGrid.setFullHeight(true);
        dataGrid.setSelectionMode(SelectionMode.DISABLED);
        return dataGrid;
    }

    public void syncUiFromModel(HasPersonDetails p) {
        model = p;
        if (p instanceof Entity)
            setLoadingStore(((Entity) p).getStore());
        firstNameTextField.setText(p.getFirstName());
        lastNameTextField.setText(p.getLastName());
        maleRadioButton.setSelected(Booleans.isTrue(p.isMale()));
        femaleRadioButton.setSelected(Booleans.isFalse(p.isMale()));
        LocalDate birthDate = null;
        if (p instanceof Person) {
            Person person = (Person) p;
            birthDate = person.getBirthDate();
            person.setAge(computeAge(birthDate));
        }
        birthDatePicker.setValue(birthDate);
        Integer age = p.getAge();
        adultRadioButton.setSelected(age == null || age > CHILD_MAX_AGE);
        childRadioButton.setSelected((age != null && age <= CHILD_MAX_AGE));
        carer1NameTextField.setText(p.getCarer1Name());
        carer2NameTextField.setText(p.getCarer2Name());
        emailTextField.setText(p.getEmail());
        phoneTextField.setText(p.getPhone());
        streetTextField.setText(p.getStreet());
        postCodeTextField.setText(p.getPostCode());
        cityNameTextField.setText(p.getCityName());
        organizationSelector.setEntity(p.getOrganization());
        countrySelector.setEntity(p.getCountry());
        updateUiEditable();
        if (sectionPanel.getCenter() == null)
            Properties.runNowAndOnPropertiesChange(pty -> updatePanelBody(), childRadioButton.selectedProperty(), i18n.dictionaryProperty());
        if (!editable)
            Toolkit.get().scheduler().runInUiThread(this::updatePanelBody);
    }

    public void syncModelFromUi(HasPersonDetails p) {
        p.setFirstName(firstNameTextField.getText());
        p.setLastName(lastNameTextField.getText());
        p.setMale(maleRadioButton.isSelected());
        p.setAge(childRadioButton.isSelected() ? computeAge(birthDatePicker.getValue()) : null);
        p.setCarer1Name(carer1NameTextField.getText());
        p.setCarer2Name(carer2NameTextField.getText());
        p.setEmail(emailTextField.getText());
        p.setPhone(phoneTextField.getText());
        p.setStreet(streetTextField.getText());
        p.setPostCode(postCodeTextField.getText());
        p.setCityName(cityNameTextField.getText());
        p.setOrganization(organizationSelector.getEntity());
        p.setCountry(countrySelector.getEntity());
        Country country = p.getCountry();
        p.setCountryName(country == null ? null : country.getName());
    }

    private Integer computeAge(LocalDate birthDate) {
        Integer age = null;
        if (birthDate != null) {
            // Integer age = (int) birthDate.until(event.getStartDate(), ChronoUnit.YEARS); // Doesn't compile with GWT
            age = (int) (event.getStartDate().toEpochDay() - birthDate.toEpochDay()) / 365;
            if (age >= 18) // TODO: move this later in a applyBusinessRules() method
                age = null;
        }
        return age;
    }
}
