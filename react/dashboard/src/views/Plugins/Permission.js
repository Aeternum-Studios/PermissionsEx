import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'

import { success, error } from '../../reducers/notificationReducer'
import { copyToClipboard } from '../../utils/clipboard'

import Icon from '../../components/Icon'

import {
    ListGroupItem,
    Button,
    Media
} from 'reactstrap'

class Permission extends React.Component {

    render() {
        const permission = this.props.permission

        const clipboardButton = {
            backgroundColor: '#66bb6a',
            color: '#fff',
            padding: '12px 15px',
            borderColor: '#66bb6a'
        }

        const negated = {
            backgroundColor: '#ba68c8',
            color: '#fff',
            padding: '12px 15px',
            borderColor: '#ba68c8'
        }

        const id = (this.props.pluginName + '_' + permission + '_').replace(' ', '')

        return (
            <Media>
                <Media left>
                    <Button title='Copy permission to Clipboard' id={id + 'clip'} style={clipboardButton}
                        onClick={() => copyToClipboard(permission, this.props.success, this.props.error)}><Icon i='fa fa-clone' />
                    </Button>
                </Media>
                <Media left>
                    <Button title='Copy negated permission to Clipboard' id={id + 'nclip'} style={negated}
                        onClick={() => copyToClipboard('-' + permission, this.props.success, this.props.error)}><Icon i='fa fa-clone' />
                    </Button>
                </Media>
                <Media body>
                    <ListGroupItem color="secondary" >
                        {permission}
                    </ListGroupItem>
                </Media>
            </Media>
        )
    }
}

Permission.contextTypes = {
    store: PropTypes.object
}

export default connect(
    null, { success, error }
)(Permission)