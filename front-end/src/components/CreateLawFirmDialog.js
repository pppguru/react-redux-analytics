// Copyright (c) 2017 Practice Insight Pty Ltd. All rights reserved.
//jshint esversion:6
//@flow
import u from "updeep"
import React from "react"
import ReactDOM from "react-dom"
import "dialog-polyfill/dialog-polyfill.css"
import * as DialogPolyfill from "dialog-polyfill"
import {
  Button,
  Cell,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  IconButton,
  ProgressBar,
  Textfield
} from "react-mdl"
import onClickOutside from "react-onclickoutside"
import "../styles/main.scss"
import SelectField from "./Selectfield"

const deStates = {
  "BY": "Bayern / Bavaria (old code=BAY)",
  "NV": "Nordrhein-Westfalen / North Rhine-Westphalia (old code=NRHE-WFA)",
  "BW": "Baden-Württemberg / Baden-Wuerttemberg (old code=BAD-WUE)",
  "HH": "Hamburg / Hamburg (old code=HAM)",
  "NI": "Niedersachsen / Lower Saxony (old code=NSAC)",
  "SN": "Sachsen / Saxony (old code=SAC)",
  "TH": "Thüringen / Thuringia (old code=THE)",
  "HE": "Hessen / Hesse (old code=HES)",
  "HB": "Bremen / Bremen (old code=BRE)",
  "SL": "Saarland / Saarland (old code=SAA)",
  "BE": "Berlin / Berlin (old code=BER)",
  "RP": "Rheinland-Pfalz / Rhineland-Palatinate (old code=RHE-PFA)",
  "ST": "Sachsen-Anhalt / Saxony-Anhalt (old code=SAC-ANH)",
  "MV": "Mecklenburg-Vorpommern / Mecklenburg-Western Pomerania (old code=MEC-VPOM)",
  "SH": "Schleswig-Holstein / Schleswig-Holstein (old code=SCN)",
  "BB": "Brandenburg / Brandenburg (old code=BRG)",
}

const gbStates = {
  "CHI": "Channel",
  "ENG": "England",
  "IOM": "Isle of Man",
  "NIR": "Northern Ireland",
  "SCT": "Scotland",
  "WLS": "Wales",
}

const usStates = {
  "AL": "Alabama",
  "AK": "Alaska",
  "AS": "American Samoa",
  "AZ": "Arizona",
  "AR": "Arkansas",
  "CA": "California",
  "CO": "Colorado",
  "CT": "Connecticut",
  "DE": "Delaware",
  "DC": "District Of Columbia",
  "FM": "Federated States Of Micronesia",
  "FL": "Florida",
  "GA": "Georgia",
  "GU": "Guam",
  "HI": "Hawaii",
  "ID": "Idaho",
  "IL": "Illinois",
  "IN": "Indiana",
  "IA": "Iowa",
  "KS": "Kansas",
  "KY": "Kentucky",
  "LA": "Louisiana",
  "ME": "Maine",
  "MH": "Marshall Islands",
  "MD": "Maryland",
  "MA": "Massachusetts",
  "MI": "Michigan",
  "MN": "Minnesota",
  "MS": "Mississippi",
  "MO": "Missouri",
  "MT": "Montana",
  "NE": "Nebraska",
  "NV": "Nevada",
  "NH": "New Hampshire",
  "NJ": "New Jersey",
  "NM": "New Mexico",
  "NY": "New York",
  "NC": "North Carolina",
  "ND": "North Dakota",
  "MP": "Northern Mariana Islands",
  "OH": "Ohio",
  "OK": "Oklahoma",
  "OR": "Oregon",
  "PW": "Palau",
  "PA": "Pennsylvania",
  "PR": "Puerto Rico",
  "RI": "Rhode Island",
  "SC": "South Carolina",
  "SD": "South Dakota",
  "TN": "Tennessee",
  "TX": "Texas",
  "UT": "Utah",
  "VT": "Vermont",
  "VI": "Virgin Islands",
  "VA": "Virginia",
  "WA": "Washington",
  "WV": "West Virginia",
  "WI": "Wisconsin",
  "WY": "Wyoming",
}

type CreateLawFirmDialogProps = {
  value: Object,
  open: boolean,
  loading: boolean,
  onSubmit: (firm: Object) => void,
  onClose: () => void
}

class CreateLawFirmDialog extends React.Component {
  props: CreateLawFirmDialogProps;
  state: Object;
  initialState: Object;
  nameInput: any;

  constructor(props: CreateLawFirmDialogProps) {
    super(props)
    this.initialState = {
      name: "",
      websiteUrl: "",
      state: "",
      countryCode: this.props.value.serviceAddressToSort.country,
      serviceAddress: this.props.value.serviceAddressToSort,
    }
    this.state = u({}, this.initialState)
  }

  componentWillReceiveProps(nextProps: CreateLawFirmDialogProps) {
    if (!this.props.open && nextProps.open) {
      this.setState(u({}, this.initialState))
    }
  }

  componentDidMount() {
    const dialog = ReactDOM.findDOMNode(this)
    if (dialog && !dialog.showModal) {
      DialogPolyfill.registerDialog(dialog)
    }
  }

  createListener(name: string) {
    return (event) => {
      this.setState(u({[name]: event.target.value}, this.state))
    }
  }

  handleClickOutside = (event: Event) => {
    if (this.props.open) {
      this.setState({name: "", websiteUrl: "", state: ""})
      this.props.onClose()
    }
  }

  isValid = (): boolean => {
    if (!this.state.name) {
      return false
    }
    const countryCode = this.props.value.serviceAddressToSort.country
    const stateRequired = countryCode === "US" || countryCode === "GB" || countryCode === "DE"
    return !stateRequired || this.state.state !== ""
  }

  getOptions = (countryCode: string) => {
    const createOption = ([key, value]) => ({ value: key, name: `${key} - ${String(value)}`})
    if (countryCode === "US") {
      return Object.entries(usStates).map(createOption)
    }
    if (countryCode === "GB") {
      return Object.entries(gbStates).map(createOption)
    }
    if (countryCode === "DE") {
      return Object.entries(deStates).map(createOption)
    }
    return null
  }

  getDefaultOption = (countryCode: string): string => {
    if (countryCode === "US" || countryCode === "GB" || countryCode === "DE") {
      return ""
    }
    return "All States"
  }

  render(): ?React.Element<any> {

    const close = (event: Event) => {
      if (event) {
        event.preventDefault()
      }
      this.setState({name: "", websiteUrl: "", state: ""})
      this.props.onClose()
    }

    const progress = this.props.loading ? (<ProgressBar indeterminate/>) : null

    return (
      <Dialog open={this.props.open} onCancel={close}>
        <DialogTitle>Create new Law Firm</DialogTitle>
        <IconButton disabled={this.props.loading} name="close" onClick={close} style={{top: "18px"}}
                    onFocus={(event) => this.nameInput.inputRef.focus()}>Close</IconButton>
        {progress}
        <form onSubmit={(event: Event) => {
          event.preventDefault()
          this.props.onSubmit(this.state)
        }}>
          <DialogContent>
            <div style={{padding: "0px 16px"}}>{"For service address: " + this.props.value.serviceAddressToSort.name + ", "
                  + this.props.value.serviceAddressToSort.address}</div>
            <Grid>
              <Cell col={12}>
                <Textfield value={this.state.name} disabled={this.props.loading} style={{width: "100%"}} id={"name"}
                           label={"Firm Name"} ref={(input) => {
                  this.nameInput = input
                }} onChange={this.createListener("name")}/>
              </Cell>
            </Grid>
            <Grid>
              <Cell col={12}>
                <Textfield value={this.state.websiteUrl} disabled={this.props.loading} style={{width: "100%"}} id={"url"}
                           label={"Firm URL"}
                           onChange={this.createListener("websiteUrl")}/>
              </Cell>
            </Grid>
            <Grid>
              <Cell col={6}>
                <Textfield readOnly={true} label={"Select country"} value={this.props.value.serviceAddressToSort.country}
                           onChange={this.createListener("countryCode")}/>
              </Cell>
              <Cell col={6} style={{textAlign: "right"}}>
                <SelectField value={this.state.state}
                             defaultOption= {this.getDefaultOption(this.props.value.serviceAddressToSort.country)}
                             label="Select state" id="countryState" name="countryState"
                             options={this.getOptions(this.props.value.serviceAddressToSort.country)}
                             onChange={this.createListener("state")}/>
              </Cell>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button type="button" disabled={this.props.loading} onClick={close}>Cancel</Button>
            <Button type="submit" disabled={this.props.loading || !this.isValid()}>Save</Button>
          </DialogActions>
        </form>
      </Dialog>
    )
  }
}

export default onClickOutside(CreateLawFirmDialog)
